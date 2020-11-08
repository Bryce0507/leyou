package com.leyou.search.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.leyou.common.vo.PageResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.request.SearchRequest;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.directory.SearchResult;
import java.util.List;
import java.util.Map;

@RestController
public class SearchController {
    @Autowired
    private SearchService searchService;

    /**
     * 商品分页查询
     * @param request
     * @return
     */
    @PostMapping("page")
    public ResponseEntity<PageResult<Goods>> search(@RequestBody SearchRequest request) {
        return ResponseEntity.ok(searchService.search(request));
    }


    /**
     * 查询过滤项
     * @param request
     * @return
     */
    @PostMapping("filter")
    public ResponseEntity<Map<String, List<?>>> queryFilter(@RequestBody SearchRequest request)  {
        return ResponseEntity.ok(searchService.queryFilter(request));
    }


}
