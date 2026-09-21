package com.example.bookshelf.service;

import com.example.bookshelf.exception.BookNotFoundException;
import com.example.bookshelf.exception.DuplicateBookException;
import com.example.bookshelf.exception.InvalidBookException;
import com.example.bookshelf.model.Book;
import com.example.bookshelf.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bookService = new BookService(bookRepository);
    }

    @Test
    void createSavesBookWhenValidAndNotDuplicate() {
        when(bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase("Dune", "Frank Herbert")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.create("Dune", "Frank Herbert");

        assertThat(result.getTitle()).isEqualTo("Dune");
        assertThat(result.getAuthor()).isEqualTo("Frank Herbert");
    }

    @Test
    void createThrowsInvalidBookExceptionWhenTitleBlank() {
        assertThatThrownBy(() -> bookService.create(" ", "Frank Herbert"))
                .isInstanceOf(InvalidBookException.class)
                .hasMessage("please check the request payload");
    }

    @Test
    void createThrowsInvalidBookExceptionWhenFieldTooLong() {
        String tooLong = "a".repeat(256);
        assertThatThrownBy(() -> bookService.create(tooLong, "Frank Herbert"))
                .isInstanceOf(InvalidBookException.class);
    }

    @Test
    void createThrowsDuplicateBookExceptionWhenAlreadyExists() {
        when(bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase("Dune", "Frank Herbert")).thenReturn(true);

        assertThatThrownBy(() -> bookService.create("Dune", "Frank Herbert"))
                .isInstanceOf(DuplicateBookException.class)
                .hasMessage("record already exist");
    }

    @Test
    void findAllWithNoFiltersReturnsAllBooks() {
        bookService.findAll(null, null);
        verify(bookRepository).findAll();
    }

    @Test
    void findAllTreatsBlankFilterAsAbsent() {
        bookService.findAll("  ", "");
        verify(bookRepository).findAll();
    }

    @Test
    void findAllWithTitleOnlyUsesTitleQuery() {
        bookService.findAll("Dune", null);
        verify(bookRepository).findByTitleContainingIgnoreCase("Dune");
    }

    @Test
    void findAllWithAuthorOnlyUsesAuthorQuery() {
        bookService.findAll(null, "Herbert");
        verify(bookRepository).findByAuthorContainingIgnoreCase("Herbert");
    }

    @Test
    void findAllWithBothFiltersUsesCombinedQuery() {
        bookService.findAll("Dune", "Herbert");
        verify(bookRepository).findByTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase("Dune", "Herbert");
    }

    @Test
    void updateAuthorUpdatesExistingBook() {
        Book existing = new Book("Dune", "Old Author");
        when(bookRepository.findById("id-1")).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book updated = bookService.updateAuthor("id-1", "New Author");

        assertThat(updated.getAuthor()).isEqualTo("New Author");
        assertThat(updated.getTitle()).isEqualTo("Dune");
    }

    @Test
    void updateAuthorThrowsNotFoundWhenIdMissing() {
        when(bookRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateAuthor("missing", "New Author"))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("no book present");
    }

    @Test
    void updateAuthorThrowsInvalidWhenAuthorBlank() {
        assertThatThrownBy(() -> bookService.updateAuthor("id-1", ""))
                .isInstanceOf(InvalidBookException.class);
    }

    @Test
    void deleteRemovesExistingBook() {
        Book existing = new Book("Dune", "Frank Herbert");
        when(bookRepository.findById("id-1")).thenReturn(Optional.of(existing));

        bookService.delete("id-1");

        verify(bookRepository).delete(existing);
    }

    @Test
    void deleteThrowsNotFoundWhenIdMissing() {
        when(bookRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.delete("missing"))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("no book present");
    }
}
