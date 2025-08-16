package me.xyzo.blackwatchBE.exception;

public class MongoApiTimeoutException extends MongoApiException {
    public MongoApiTimeoutException(String message) {
        super(message, null, 408);
    }
}
