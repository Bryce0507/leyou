package com.leyou.client;

import com.leyou.LySearchApplication;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.CategoryDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchApplication.class)
public class CategoryClientTest {

    @Autowired
    private ItemClient itemClient;

    @Test
    public void queryByIdList() {
        List<CategoryDTO> list = itemClient.queryCategoryByIds(Arrays.asList(1l, 2l, 3l));
        for (CategoryDTO category : list) {
            System.out.println("category = " + category);
        }
        Assert.assertEquals(3, list.size());
        
    }
}
