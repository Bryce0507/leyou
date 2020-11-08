package com.leyou.page.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PageServiceTest {

    @Autowired
    private PageService pageService;

    @Test
    public void createItemHtml() throws InterruptedException {
        List<Long> list = new ArrayList<>();
        for (int i = 2; i <183 ; i++) {
            list.add(Long.valueOf(i));
        }

        for (Long id : list) {
            pageService.createItemHtml(id);
            Thread.sleep(500);
        }
    }
}
