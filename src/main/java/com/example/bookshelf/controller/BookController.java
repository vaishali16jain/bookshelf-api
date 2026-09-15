package com.example.bookshelf.controller;

import com.example.bookshelf.model.Book;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/books")
public class BookController {

    // In-memory store for the prototype — intentionally not persistent.
    private final List<Book> books = new CopyOnWriteArrayList<>();

    @GetMapping
    public List<Book> getAllBooks() {
        return books;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book addBook(@RequestBody Book incoming) {
        // NOTE: no validation yet — this is exactly the gap US-101.1
        // (the amendment) is meant to close. Left as-is on purpose so the
        // design-review step in the SDLC has something real to catch.
        Book book = new Book(incoming.getTitle(), incoming.getAuthor());
        books.add(book);
        return book;
    }
}
