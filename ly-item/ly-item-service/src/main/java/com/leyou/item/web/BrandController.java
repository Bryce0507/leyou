package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;


    @GetMapping("page")
    public ResponseEntity<PageResult<BrandDTO>> queryBrandByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "sortBy",required = false)String sortBy,
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "desc",defaultValue = "false")Boolean desc
            ) {
        return ResponseEntity
                .ok(brandService.queryBrandByPage(page, rows, sortBy, key, desc));
    }


    /**
     * 新增品牌
     * @param brandDTO
     * @param ids
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(BrandDTO brandDTO, @RequestParam("cids") List<Long> ids) {
        brandService.saveBrand(brandDTO, ids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 跟新按钮
     * @param brandDTO
     * @param ids
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateBrand(BrandDTO brandDTO, @RequestParam("cids") List<Long> ids) {
        brandService.updateBrand(brandDTO, ids);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteBrand(@RequestParam("bid") Long bid) {
        brandService.deleteBrand(bid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/of/category")
    public ResponseEntity<List<BrandDTO>> queryBrandByCid(@RequestParam("id") Long cid) {
        return ResponseEntity.ok(brandService.queryBrandByCid(cid));
    }

    @GetMapping({"{id}"})
    public ResponseEntity<BrandDTO> queryById(@PathVariable("id") Long id) {
        BrandDTO brandDTO = brandService.queryById(id);
        return ResponseEntity.ok(brandDTO);
    }


    /**
     * 根据id聚合查询品牌对象集合
     * @param ids
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<BrandDTO>> queryBrandsByIds(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(brandService.queryBrandsByIds(ids));
    }

}
