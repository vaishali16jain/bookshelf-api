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
- Updated the dependency baseline to Spring Boot 4.0.8 while retaining Java 17; aligned Jackson, JUnit Platform, and Surefire versions, and updated test compatibility for Boot 4/Jackson 3 and RestAssured 6 with Groovy 5.

### Verification

- Current Step 7 runs passed: 16 service tests, 24 controller tests, 7 functional tests, and the full 40 JUnit + 7 TestNG suite; all reported 0 failures, 0 errors, and 0 skipped.
- Packaged API smoke passed (GET 200, POST 201, GET created record, DELETE 204, cleanup GET 200); compiler release is 17.
- Fresh OSV audit queried all 176 resolved project dependency coordinates (75 production, 101 test) and returned 0 matching coordinates, 0 advisory records, and 0 unique advisory IDs. Maven build plugins, alternate profiles, and platform-specific graphs were outside audit scope.
