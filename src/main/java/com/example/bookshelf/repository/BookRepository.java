package com.example.bookshelf.repository;

import com.example.bookshelf.model.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, String> {

  boolean existsByTitleIgnoreCaseAndAuthorIgnoreCase(String title, String author);

  List<Book> findByTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(
      String title, String author);

  List<Book> findByTitleContainingIgnoreCase(String title);

  List<Book> findByAuthorContainingIgnoreCase(String author);
}
