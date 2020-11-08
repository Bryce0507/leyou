package com.leyou.order.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.threadLocals.UserHolder;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SkuDTO;
import com.leyou.order.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.entity.Order;
import com.leyou.order.entity.OrderDetail;
import com.leyou.order.entity.OrderLogistics;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderLogisticsMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.utils.PayHelper;
import com.leyou.order.vo.OrderDetailVO;
import com.leyou.order.vo.OrderLogisticsVO;
import com.leyou.order.vo.OrderVO;
import com.leyou.user.AddressDTO;
import com.leyou.user.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    private static final String KEY_PATTERN = "ly:pay:orderId";
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private UserClient userClient;

    @Autowired
    private OrderLogisticsMapper logisticsMapper;


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PayHelper payHelper;
    

    /**
     * 创建订单
     * @param orderDTO
     * @return  订单编号
     */
    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
        //1.写订单
        Order order = new Order();
        //1.1订单编号
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        //1.2登录用户
        Long userId = UserHolder.getUser();
        order.setUserId(userId);
        //1.3 金额相关
        @NotNull List<CartDTO> carts = orderDTO.getCarts();
        //获取所有的skuId
        List<Long> skuIdList = carts.stream().map(CartDTO::getSkuId).collect(Collectors.toList());
        //根据skuIdList 查询skuList
        List<SkuDTO> skuList = itemClient.querySkuByIds(skuIdList);
        //获取skuId 对应的num 的map
        Map<Long, Integer> map = carts.stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        //定义一个OrderDetail的集合
        List<OrderDetail> details = new ArrayList<>();
        Long total = 0L;
        for (SkuDTO skuDTO : skuList) {
            total += skuDTO.getPrice() * map.get(skuDTO.getId());
            //组装orderDetail
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(orderId);
            detail.setSkuId(skuDTO.getId());
            detail.setNum(map.get(skuDTO.getId()));
            detail.setTitle(skuDTO.getTitle());
            detail.setOwnSpec(skuDTO.getOwnSpec());
            detail.setPrice(skuDTO.getPrice());
            detail.setImage(StringUtils.substringBefore(skuDTO.getImages(), ","));
            //将detail塞进details
            details.add(detail);
        }
        order.setTotalFee(total);
        //付款方式
        order.setPaymentType(orderDTO.getPaymentType());
        //实付
        order.setActualFee(total + order.getPostFee() /* -优惠金额*/);

        //1.4订单初始化
        order.setStatus(OrderStatusEnum.INIT.value());

        //1.5写order到数据库
        int count = orderMapper.insertSelective(order);
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        //2.写orderDetail
        count = detailMapper.insertDetailList(details);
        if (count != details.size()) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        //3.写入OrderLogistics
        //3.1查询收货地址
        AddressDTO addressDTO = userClient.queryAddressById(userId, orderDTO.getAddressId());
        //3.2填写物流信息
        OrderLogistics orderLogistics = BeanHelper.copyProperties(addressDTO, OrderLogistics.class);
        orderLogistics.setOrderId(orderId); //注意userClient查询返回的addressDTO中并没有订单id  所以不设置的话 会报错

        count = logisticsMapper.insertSelective(orderLogistics);
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //生成订单后 需要减库存
        itemClient.minusStock(map);

        //TODO   删除购物车中 已下单的 商品信息
        return orderId;


    }

    /**
     * 根据订单的id 查询订单的详情
     * @param id
     * @return
     */
    public OrderVO queryOrderById(Long id) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        //判断用户id是否正确
        Long userId = UserHolder.getUser();
        if (!userId.equals(order.getUserId())) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }

        //查看商品详情
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(id);
        List<OrderDetail> details = detailMapper.select(orderDetail);
        if (CollectionUtils.isEmpty(details)) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }

        //查看订单的状态
        OrderLogistics orderLogistics = logisticsMapper.selectByPrimaryKey(id);
        if (orderLogistics == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }

        //封装数据
        OrderVO orderVO = BeanHelper.copyProperties(order, OrderVO.class);
        List<OrderDetailVO> orderDetailVOS = BeanHelper.copyWithCollection(details, OrderDetailVO.class);
        OrderLogisticsVO orderLogisticsVO = BeanHelper.copyProperties(orderLogistics, OrderLogisticsVO.class);
        orderVO.setDetailList(orderDetailVOS);
        orderVO.setLogistics(orderLogisticsVO);
        return orderVO;

    }

    /**
     * 生成支付二维码
     * @param orderId
     * @return
     */
    public String createPayUrl(Long orderId) {

        //看是否已经生成
        String key = String.format(KEY_PATTERN, orderId);

        String cacheUrl = redisTemplate.opsForValue().get(key);
        //url有效时间为2小时，如果redis 中存在 就直接返回
        if (StringUtils.isNoneBlank(cacheUrl)) {
            return cacheUrl;
        }
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }

        //判断订单状态
        Integer status = order.getStatus();
        if (!status.equals(OrderStatusEnum.INIT.value())) {
            //订单支付过了
            throw new LyException(ExceptionEnum.INVALID_ORDER_STATUS);
        }

        //支付金额，测试时写1
        Long actualFee = /*order.getActualFee()*/1L;

        //商品描述
        String desc = "【乐优商城】商品信息";
        String url = payHelper.createOrder(orderId, actualFee, desc);

        //存入redis中，有效时间为2小时
        redisTemplate.opsForValue().set(key, url,2, TimeUnit.HOURS);
        return url;
    }

    /**
     * 处理回调
     * @param result
     */
    @Transactional
    public void handleNotify(Map<String, String> result) {
        //1.签名校验
        try {
            payHelper.isValidSign(result);
        } catch (Exception e) {
            log.error("【微信回调】微信签名有误！，result：{}", result, e);
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_SIGN, e);
        }

        //2.业务校验
        payHelper.checkResultCode(result);
        //3.校验金额数据
        String totalFeeStr = result.get("total_fee");
        String orderIdStr = result.get("out_trade_no");
        if (StringUtils.isBlank(totalFeeStr) || StringUtils.isBlank(orderIdStr)) {
            //回调参数中必须包含订单的金额和订单编号
        }
        //3.1获取结果中的金额
        Long totalFee = Long.valueOf(totalFeeStr);
        Long orderId = Long.valueOf(orderIdStr);

        //3.2获取订单
        Order order = orderMapper.selectByPrimaryKey(orderId);

        //3.3判断订单的状态，保证幂等
        if (order.getStatus().equals(OrderStatusEnum.INIT.value())) {
            //订单已经支付
            return;  //这里直接return 而不是抛异常  是为了让微信知道 我们已经收到回调了 而不需要因为异常重新发送回调
        }

        //3.4判断金额是否一致
        if (totalFee != 1L/*order.getActualFee()*/) {
            //金额不符合
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
        }

        //4修改订单的状态

        Order record = new Order();
        record.setStatus(OrderStatusEnum.PAY_UP.value());
        record.setOrderId(orderId);
        order.setPayTime(new Date());
        int count = orderMapper.updateByPrimaryKeySelective(record);
        if (count != 1) {
            log.error("【微信回调】更新订单状态失败，订单id：{}", orderId);
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        log.info("【微信回调】订单支付成功！编号id：{}", orderId);
    }

    /**
     * 查询订单的支付状态
     *
     * @param orderId
     * @return
     */
    public Integer queryPayStatus(Long orderId) {
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        return order.getStatus();
    }
}
