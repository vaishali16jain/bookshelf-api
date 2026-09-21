package com.example.bookshelf.service;

import com.example.bookshelf.exception.BookNotFoundException;
import com.example.bookshelf.exception.DuplicateBookException;
import com.example.bookshelf.exception.InvalidBookException;
import com.example.bookshelf.model.Book;
import com.example.bookshelf.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private static final int MAX_LENGTH = 255;

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book create(String title, String author) {
        validateField(title);
        validateField(author);
        if (bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(title, author)) {
            throw new DuplicateBookException();
        }
        return bookRepository.save(new Book(title, author));
    }

    public List<Book> findAll(String titleFilter, String authorFilter) {
        boolean hasTitle = titleFilter != null && !titleFilter.isBlank();
        boolean hasAuthor = authorFilter != null && !authorFilter.isBlank();
        if (hasTitle && hasAuthor) {
            return bookRepository.findByTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(titleFilter, authorFilter);
        }
        if (hasTitle) {
            return bookRepository.findByTitleContainingIgnoreCase(titleFilter);
        }
        if (hasAuthor) {
            return bookRepository.findByAuthorContainingIgnoreCase(authorFilter);
        }
        return bookRepository.findAll();
    }

    public Book updateAuthor(String id, String author) {
        validateField(author);
        Book book = bookRepository.findById(id).orElseThrow(BookNotFoundException::new);
        book.setAuthor(author);
        return bookRepository.save(book);
    }

    public void delete(String id) {
        Book book = bookRepository.findById(id).orElseThrow(BookNotFoundException::new);
        bookRepository.delete(book);
    }

    private void validateField(String value) {
        if (value == null || value.isBlank() || value.length() > MAX_LENGTH) {
            throw new InvalidBookException();
        }
    }
}
