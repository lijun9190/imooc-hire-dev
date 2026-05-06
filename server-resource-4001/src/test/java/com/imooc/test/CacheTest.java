package com.imooc.test;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class CacheTest {

    @Test
    public void testCache() throws Exception {

        // 构建缓存对象
        Cache<String, Object> cache = Caffeine.newBuilder()
                //.maximumSize(1)     // 缓存个数，达到阈值则开始淘汰
                .expireAfterWrite(5, TimeUnit.SECONDS)      // 在最后写入后的存在5秒的有效期
                .build();

        // 向本地缓存中存储数据
        cache.put("name", "慕课网");
        cache.put("age", 18);
        cache.put("sex", "boy");

        // 获得数据
        String name = cache.getIfPresent("name").toString();
        System.out.println("name = " + name);

        String age = cache.getIfPresent("age").toString();
        System.out.println("age = " + age);

        String sex = cache.getIfPresent("sex").toString();
        System.out.println("sex = " + sex);

        //String birthday = cache.getIfPresent("birthday").toString();
        //System.out.println("birthday = " + birthday);


        String birthday = cache.get("birthday", s -> {
            // 如果没有命中缓存，可以去数据库中查询，再返回给前端
            // 假设此处从DB中查询并且获得birthday
            return "2025-12-25";
        }).toString();
        System.out.println("birthday = " + birthday);

        String birthday2 = cache.getIfPresent("birthday").toString();
        System.out.println("birthday = " + birthday2);

        System.out.println("+++++++++++++++++++++++++++++");
        Object name1 = cache.getIfPresent("name");
        System.out.println("name = " + name1);

        Object age1 = cache.getIfPresent("age");
        System.out.println("age = " + age1);

        Object sex1 = cache.getIfPresent("sex");
        System.out.println("sex = " + sex1);

        Object birthday1 = cache.getIfPresent("birthday");
        System.out.println("birthday = " + birthday1);

        System.out.println("+++++++++++++++++++++++++++++");
        Thread.sleep(2000);
        Object name2 = cache.getIfPresent("name");
        System.out.println("name = " + name2);

        Object age2 = cache.getIfPresent("age");
        System.out.println("age = " + age2);

        Object sex2 = cache.getIfPresent("sex");
        System.out.println("sex = " + sex2);

        cache.put("age", 20);

        Thread.sleep(3000);
        Object name3 = cache.getIfPresent("name");
        System.out.println("name = " + name3);

        Object age3 = cache.getIfPresent("age");
        System.out.println("age = " + age3);

        Object sex3 = cache.getIfPresent("sex");
        System.out.println("sex = " + sex3);
    }


}
