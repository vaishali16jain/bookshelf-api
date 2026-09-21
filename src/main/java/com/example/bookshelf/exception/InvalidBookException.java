package com.example.bookshelf.exception;

public class InvalidBookException extends RuntimeException {
    public InvalidBookException() {
        super("please check the request payload");
    }
}
