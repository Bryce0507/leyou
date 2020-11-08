package com.leyou.cart.service;

import com.leyou.cart.entity.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.threadLocals.UserHolder;
import com.leyou.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "ly:cart:uid:";

    /**
     * 添加商品到购物车
     * @param cart
     */
    public void addCart(Cart cart) {
        //获取当前用户
        String key = KEY_PREFIX + UserHolder.getUser();
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
/*
        //获取商品的id
        String hashKey = cart.getSkuId().toString();
        //获取数量
        Integer num = cart.getNum();
        //判断添加的商品是否存在
        Boolean boo = hashOps.hasKey(hashKey);
        if (boo != null && boo) {
            //存在
            cart = JsonUtils.toBean(hashOps.get(hashKey), Cart.class);
            cart.setNum(cart.getNum()+num);
        }
        //写入redis
        hashOps.put(hashKey, JsonUtils.toString(cart));*/
        addCartToRedis(cart, hashOps);


    }

    /**
     * 查询购物车列表
     * @return
     */
    public List<Cart> queryCartList() {
        //获取登录用户名
        String key = KEY_PREFIX + UserHolder.getUser();
        //判断购物车中是否存在
        Boolean boo = redisTemplate.hasKey(key);
        if (boo == null || !boo) {
            //不存在，直接返回
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }

        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        //判断是否有数据
        Long size = hashOps.size();
        if (size == null || size < 0) {
            //不存在直接返回
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        List<String> carts = hashOps.values();

        //TODO  遍历判断
        //查询购物数据
        return carts.stream()
                .map(json -> JsonUtils.toBean(json, Cart.class))
                .collect(Collectors.toList());


    }

    /**
     * 更改购物车的商品数量
     * @param skuId
     * @param num
     */
    public void updateNum(Long skuId, Integer num) {
        //获取当前用户
        Long userId = UserHolder.getUser();
        String key = KEY_PREFIX + userId;

        //获取hash操作的对象
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);

        //判断是否存在
        Boolean boo = hashOps.hasKey(skuId.toString());
        if (boo == null || !boo) {
            log.error("购物车商品不存在，用户：{}，商品：{}",userId,skuId);
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        //查询购物车数据
        Cart cart = JsonUtils.toBean(hashOps.get(skuId.toString()), Cart.class);
        //查询修改
        cart.setNum(num);
        //写回redis 中
        hashOps.put(skuId.toString(), JsonUtils.toString(cart));

    }

    /**
     * 删除购物车商品
     *
     * @param skuId
     */
    public void deleteCart(String skuId) {
        Long userId = UserHolder.getUser();
        String key = KEY_PREFIX + userId;
        redisTemplate.opsForHash().delete(key, skuId);
    }

    /**
     * 批量新增到购物车
     * @param cartList
     */
    public void addCartList(List<Cart> cartList) {
        //获取
        Long userId = UserHolder.getUser();
        String key = KEY_PREFIX + userId;
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        cartList.forEach(cart -> addCartToRedis(cart, hashOps));

    }

    /**
     * 单个商品添加到redis中
     * @param cart
     * @param hashOps
     */
    private void addCartToRedis(Cart cart, BoundHashOperations<String, String, String> hashOps) {
        //获取skuId
        String skuId = cart.getSkuId().toString();
        //获取数量
        Integer num = cart.getNum();
        //判断添加的商品是否存在
        Boolean boo = hashOps.hasKey(skuId);
        if (boo != null && boo) {
            //如果存在 改变数量
            cart.setNum(JsonUtils.toBean(hashOps.get(skuId), Cart.class).getNum() + num);
        }
        hashOps.put(skuId, JsonUtils.toString(cart));

    }


}
