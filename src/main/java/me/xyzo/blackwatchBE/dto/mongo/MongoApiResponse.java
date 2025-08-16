package me.xyzo.blackwatchBE.dto.mongo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MongoApiResponse<T> {
    private String result;
    private String reason;
    private T data;
    private MongoApiMeta meta;

    // getters, setters
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public MongoApiMeta getMeta() { return meta; }
    public void setMeta(MongoApiMeta meta) { this.meta = meta; }
}