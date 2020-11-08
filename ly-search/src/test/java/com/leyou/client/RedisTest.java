package com.leyou.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test01() {
        //存储数据
        redisTemplate.opsForValue().set("key1", "value1");
        //获取数据
        String v1 = (String) redisTemplate.opsForValue().get("key1");
        System.out.println("v1 = " + v1);

    }

    @Test
    public void Test02() {
        //存储数据，并指定剩余时间为5小时
        redisTemplate.opsForValue().set("ke2","value2",5, TimeUnit.HOURS);
    }


    @Test
    public void Test03() {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps("user");

        //操作hash数据
        hashOps.put("name","jack");
        hashOps.put("age","21");


        //获取单个数据
        Object name = hashOps.get("name");
        System.out.println("name = " + name);

        //获取所有数据
        Map<Object, Object> map = hashOps.entries();
        map.entrySet().forEach(objectObjectEntry
                -> System.out.println(objectObjectEntry.getKey() + ":" + objectObjectEntry.getValue()));


    }
}
