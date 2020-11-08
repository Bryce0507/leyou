package com.leyou.page.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpuDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PageService {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private SpringTemplateEngine templateEngine;
    @Value("${ly.static.itemDir}")
    private String itemDir;
    @Value("${ly.static.itemTemplate}")
    private String itemTemplate;

    public Map<String, Object> loadItemData(Long id) {
        //查询spu
        SpuDTO spuDTO = itemClient.querySpuById(id);
        //查询分类集合
        List<CategoryDTO> categories = itemClient.queryCategoryByIds(spuDTO.getCategoryIds());
        //查询品牌
        BrandDTO brandDTO = itemClient.queryBrandById(spuDTO.getBrandId());
        //查询规格
        List<SpecGroupDTO> specs = itemClient.querySpecsByCid(spuDTO.getCid3());
        //封装数据
        Map<String, Object> data = new HashMap<>();
        data.put("categories", categories);
        data.put("brand", brandDTO);
        data.put("spuName", spuDTO.getName());
        data.put("subTitle", spuDTO.getSubTitle());
        data.put("skus", spuDTO.getSkus());
        data.put("detail", spuDTO.getSpuDetail());
        data.put("specs", specs);

        return data;
    }

    /**
     * 根据spuId创建静态页面
     */
    public void createItemHtml(Long id) {
        //上下文，准备数据模型
        Context context = new Context();
        //调用之前写好的加载数据的方法
        context.setVariables(loadItemData(id));
        //准备文件路径
        File dir = new File(itemDir);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                //创建失败，抛出异常
                log.error("【静态页服务】创建静态页目录失败，目录地址：{}", dir.getAbsolutePath());
                throw new LyException(ExceptionEnum.DIRECTORY_WRITER_ERROR);
            }
        }
        File filePath = new File(dir, id + ".html");
        //准备输出流
        try {
            PrintWriter writer = new PrintWriter(filePath, "UTF-8");
            templateEngine.process(itemTemplate, context, writer);
        } catch (Exception e) {
            log.error("【静态页服务】创建静态页目录失败，目录地址：{}", dir.getAbsolutePath());
            throw new LyException(ExceptionEnum.DIRECTORY_WRITER_ERROR);
        }
    }

    /**
     * 根据spuId 删除静态页面
     * @param id
     */
    public void deleteItemHtml(Long id) {
        File file = new File(itemDir + id + ".html");
        if (file.exists()) {
            if (file.delete()) {
                log.error("【静态页服务】静态页删除失败，商品id：{}", id);
                throw new LyException(ExceptionEnum.FILE_WRITER_ERROR);
            }
        }
    }

}
