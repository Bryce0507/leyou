package com.leyou.item.web;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecController {

    @Autowired
    private SpecService specService;

    @GetMapping("/groups/of/category")
    public ResponseEntity<List<SpecGroupDTO>> queryGroupByCategory(@RequestParam("id") Long id) {
       return ResponseEntity.ok(specService.queryGroupByCategory(id));
    }

    @GetMapping("params")
    public ResponseEntity<List<SpecParamDTO>> querySpecParams(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "searching",required = false)Boolean searching
    ) {
        return ResponseEntity.ok(specService.querySpecParams(gid,cid,searching));
    }

//    @GetMapping("params")
//    public ResponseEntity<List<SpecParamDTO>> queryParamsByCid(@RequestParam("cid") Long cid) {
//        return ResponseEntity.ok(specService.queryParamsByCid(cid));
//    }


    @PostMapping("group")
    public ResponseEntity<Void> addGroup(@RequestBody SpecGroupDTO specGroupDTO){
        specService.addGroup(specGroupDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("param")
    public ResponseEntity<Void> addParam(@RequestBody SpecParamDTO specParamDTO) {
        specService.addParam(specParamDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("group")
    public ResponseEntity<Void> updateGroup(@RequestBody SpecGroupDTO specGroupDTO) {
        specService.updateGroup(specGroupDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("group/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable("id") Long id) {
        specService.deleteGroup(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 根据cid 查询参数组 和组内的参数
     * @param id
     * @return
     */
    @GetMapping("of/category")
    public ResponseEntity<List<SpecGroupDTO>> querySpecsByCid(@RequestParam("id") Long id) {
        return ResponseEntity.ok(specService.querySpecsByCid(id));
    }



}
