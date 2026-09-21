package com.example.bookshelf.controller;

import com.example.bookshelf.model.Book;
import com.example.bookshelf.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> getAllBooks(@RequestParam(required = false) String title,
                                   @RequestParam(required = false) String author) {
        return bookService.findAll(title, author);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book addBook(@RequestBody Book incoming) {
        return bookService.create(incoming.getTitle(), incoming.getAuthor());
    }

    @PutMapping("/{id}")
    public Book updateBook(@PathVariable String id, @RequestBody Book incoming) {
        return bookService.updateAuthor(id, incoming.getAuthor());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable String id) {
        bookService.delete(id);
    }
}
