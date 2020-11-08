package com.leyou.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SpuDTO;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LoadDataTest {
    @Autowired
    private SearchService searchService;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ItemClient itemClient;

    @Test
    public void loadData() {
        int page = 1, rows = 100, size = 0;
        do {
            try {
                //查询spu
                PageResult<SpuDTO> result = itemClient.querySpuByPage(page, rows, true, null);
                //取出spu
                List<SpuDTO> items = result.getItems();
                //转换
                List<Goods> goodsList = items.stream().map(searchService::buildGoods)
                        .collect(Collectors.toList());
                goodsRepository.saveAll(goodsList);
                page++;
                size = items.size();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        } while (size == 100);
    }
}
