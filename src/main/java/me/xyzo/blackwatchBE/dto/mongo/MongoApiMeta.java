package me.xyzo.blackwatchBE.dto.mongo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
class MongoApiMeta {
    private int total;
    private int page;
    private int limit;

    // getters, setters
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
}