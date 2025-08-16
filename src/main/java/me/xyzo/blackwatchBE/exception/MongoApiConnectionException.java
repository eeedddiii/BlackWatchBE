package me.xyzo.blackwatchBE.exception;

public class MongoApiConnectionException extends MongoApiException {
    public MongoApiConnectionException(String message) {
        super(message, null, 500);
    }
}
