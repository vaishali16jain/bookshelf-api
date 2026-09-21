package com.example.bookshelf.exception;

public class DuplicateBookException extends RuntimeException {
    public DuplicateBookException() {
        super("record already exist");
    }
}
