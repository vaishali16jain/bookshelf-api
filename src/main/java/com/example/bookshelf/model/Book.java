package com.example.bookshelf.model;

import java.util.UUID;

public class Book {
    private final String id;
    private String title;
    private String author;

    public Book(String title, String author) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.author = author;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
}
