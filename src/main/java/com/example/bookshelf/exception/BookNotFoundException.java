package com.example.bookshelf.exception;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException() {
        super("no book present");
    }
}
