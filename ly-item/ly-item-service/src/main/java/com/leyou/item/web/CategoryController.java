package com.leyou.item.web;

import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/of/parent")
    public ResponseEntity<List<CategoryDTO>> queryByParentId(
            @RequestParam(value = "pid", defaultValue = "0") Long pid) {
        return ResponseEntity.ok(this.categoryService.queryByParentId(pid));

    }

    @GetMapping("/of/brand")
    public ResponseEntity<List<CategoryDTO>> queryByBrandId(
            @RequestParam(value = "id") Long brandId
    ) {
        return ResponseEntity.ok(categoryService.queryByBrandId(brandId));
    }

    /**
     * 根据id的集合查询商品的分类
     * @param ids
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<CategoryDTO>> queryByIds(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(categoryService.queryByIds(ids));
    }

    /**
     * 根据3级分类id，查询1~3级的分类
     * @param cid3
     * @return
     */
    @GetMapping("/levels")
    public ResponseEntity<List<CategoryDTO>> queryAllByCid3(@RequestParam("id") Long cid3) {
        return ResponseEntity.ok(categoryService.queryAllByCid3(cid3));
    }

    /**
     * 查询一级分类
     * @return
     */
    @GetMapping("/one")
    public ResponseEntity<List<CategoryDTO>> queryOneList() {
        return ResponseEntity.ok(categoryService.queryOneList());
    }

    /**
     * 根据一级id 查对应的2级和三级
     * @param id
     * @return
     */
    @GetMapping("/two")
    public ResponseEntity<List<Map<String, List<CategoryDTO>>>> c(@RequestParam("id") Long id) {
        return ResponseEntity.ok(categoryService.twoAndThreeList(id));
    }



}
