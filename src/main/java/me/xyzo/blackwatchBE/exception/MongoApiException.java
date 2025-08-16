package me.xyzo.blackwatchBE.exception;

public class MongoApiException extends RuntimeException {
    private final String apiResponse;
    private final int statusCode;

    public MongoApiException(String message, String apiResponse, int statusCode) {
        super(message);
        this.apiResponse = apiResponse;
        this.statusCode = statusCode;
    }

    public String getApiResponse() { return apiResponse; }
    public int getStatusCode() { return statusCode; }
}
