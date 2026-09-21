# Changelog

All notable changes to this project are documented in this file.

## [Unreleased] — US-102: Filter, update, and delete books

### Added
- `GET /books?title=&author=` — filter the bookshelf by title and/or author (case-insensitive, partial match, combinable).
- `PUT /books/{id}` — update a book's `author` (`title` is immutable via this endpoint).
- `DELETE /books/{id}` — remove a book.
- Server-side validation for `POST`/`PUT`: required fields, 255-char max, case-insensitive duplicate detection on `title` + `author`.
- Centralized error handling (`GlobalExceptionHandler`) returning consistent `{"message": "..."}` bodies for `404`/`400` responses, including malformed JSON payloads.
- Unit tests (`BookServiceTest`), integration tests (`BookControllerTest`), and a functional/UI automation suite (`BookApiFunctionalTest`, REST Assured + Selenium + TestNG).

### Changed
- `BookController` now delegates all business logic to the new `BookService`; `BookRepository` moved to its own file and extended with filter/duplicate query methods.

