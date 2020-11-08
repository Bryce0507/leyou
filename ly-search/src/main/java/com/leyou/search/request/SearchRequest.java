package com.leyou.search.request;


import java.util.Map;

public class SearchRequest {
    //搜索条件
    private String key;
    //当前页
    private Integer page;

    //按照价钱排序  true为升序  ，false 为降序   null表示 不以这个排序
    private Boolean price;

    //选中的过滤项
    private Map<String, String> filter;

    // 每页大小，不从页面接收，而是固定大小
    private static final Integer DEFAULT_SIZE = 20;
    // 默认页
    private static final Integer DEFAULT_PAGE = 1;
    //默认price

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getPage() {
        if (page == null) {
            return DEFAULT_PAGE;
        }
        //页码校验 不能小于1
        return Math.max(DEFAULT_PAGE, page);
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return DEFAULT_SIZE;
    }

    public Boolean getPrice() {
        return price;
    }

    public void setPrice(Boolean price) {
        this.price = price;
    }

    public Map<String, String> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, String> filter) {
        this.filter = filter;
    }
}
