package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class SpuController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询商品
     * @param page
     * @param rows
     * @param saleable
     * @param key
     * @return
     */
    @RequestMapping("spu/page")
    public ResponseEntity<PageResult<SpuDTO>> querySpuPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows
    ) {
        return ResponseEntity.ok(goodsService.querySpuPage(key, saleable, page, rows));
    }

    @PutMapping("/spu/saleable")
    public ResponseEntity<Void> updateSpuSaleable(@RequestParam("id") Long id, @RequestParam("saleable") Boolean saleable) {
        goodsService.updateSpuSaleable(id, saleable);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/spu/detail")
    public ResponseEntity<SpuDetailDTO> querySpuDetailById(@RequestParam("id") Long id) {
        return ResponseEntity.ok(goodsService.querySpuDetailById(id));
    }

    @GetMapping("/sku/of/spu")
    public ResponseEntity<List<SkuDTO>> querySkusBySpuId(@RequestParam("id")Long id) {
        return ResponseEntity.ok(goodsService.querySkusBySpuId(id));

    }

    @DeleteMapping("/spu")
    public ResponseEntity<Void> deleteSpuById(@RequestParam("id") Long id) {
        goodsService.deleteSpuById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 根据id查询spuDTO
     * @param id
     * @return
     */
    @GetMapping("sku/{id}")
    public ResponseEntity<SpuDTO> querySpuById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }


    /**
     * 根据sku的id集合 查询sku list
     * @param ids
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<SkuDTO>> querySkuByIds(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(goodsService.querySkuByIds(ids));
    }

    /**
     * 改库存
     * @param cartMap
     * @return
     */
    @PutMapping("stock/minus")
    public ResponseEntity<Void> minusStock(@RequestBody Map<Long, Integer> cartMap) {
        goodsService.minusStock(cartMap);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }





}
