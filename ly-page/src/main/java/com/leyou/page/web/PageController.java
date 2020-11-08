package com.leyou.page.web;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @RequestMapping("item/{id}.html")
    public String toItemPage(Model model, @PathVariable("id") Long id) {
        //查询模型数据
        Map<String, Object> itemData = pageService.loadItemData(id);
        //存入模型数据，因为数据较多，直接存入一个map
        model.addAllAttributes(itemData);

        return "item";
    }
}
