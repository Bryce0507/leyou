package com.leyou.item.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(value = "item-service")
public interface ItemClient {

    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    @GetMapping("/brand/{id}")
    BrandDTO queryBrandById(@PathVariable("id") Long id);



    /**
     * 根据id的集合查询商品分类
     * @param idList 商品分类的id集合
     * @return 分类集合
     */
    @GetMapping("/category/list")
    List<CategoryDTO> queryCategoryByIds(@RequestParam("ids") List<Long> idList);


    /**
     * 分页查询spu
     * @param page 当前页
     * @param rows 每页大小
     * @param saleable 上架商品或下降商品
     * @param key 关键字
     * @return 当前页商品数据
     */
    @GetMapping("/spu/page")
    PageResult<SpuDTO> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key);


    /**
     * 根据spuID查询spuDetail
     * @param id spuID
     * @return SpuDetail
     */
    @GetMapping("/spu/detail")
    SpuDetailDTO querySpuDetailById(@RequestParam("id") Long id);

    /**
     * 根据spuID查询sku
     * @param id spuID
     * @return sku的集合
     */
    @GetMapping("sku/of/spu")
    List<SkuDTO> querySkuBySpuId(@RequestParam("id") Long id);


    /**
     * 查询规格参数
     * @param gid 组id
     * @param cid 分类id
     * @param searching 是否用于搜索
     * @return 规格组集合
     */
    @GetMapping("/spec/params")
    List<SpecParamDTO> querySpecParams(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching
    );


    /**
     * 根据id的集合查询商品的分类
     * @param ids
     * @return
     */
    @GetMapping("/category/list")
    List<CategoryDTO> queryByIds(@RequestParam("ids") List<Long> ids);


    /**
     * 根据id聚合查询品牌对象集合
     *
     * @param ids
     * @return
     */
    @GetMapping("brand/list")
    List<BrandDTO> queryBrandsByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 根据3级分类id，查询1~3级的分类
     *
     * @param cid3
     * @return
     */
    @GetMapping("category/levels/")
    List<CategoryDTO> queryAllByCid3(@RequestParam("cid3") Long cid3);


    /**
     * 根据id查询spuDTO
     *
     * @param id
     * @return
     */
    @GetMapping("sku/{id}")
    SpuDTO querySpuById(@PathVariable("id") Long id);

    /**
     * 查询规格参数组，及组内参数
     * @param id 商品分类id
     * @return 规格组及组内参数
     */
    @GetMapping("/spec/of/category")
    List<SpecGroupDTO> querySpecsByCid(@RequestParam("id") Long id);


    /**
     * 根据sku的id集合 查询sku list
     *
     * @param ids
     * @return
     */
    @GetMapping("sku/list")
    List<SkuDTO> querySkuByIds(@RequestParam("ids") List<Long> ids);


    /**
     * 减库存
     * @param cartMap  skuId 和 num 个数
     */
    @PutMapping("stock/minus")
    void minusStock(@RequestBody Map<Long, Integer> cartMap);











}
