
# Architecture

## US-101 — Add and view books

- Framework: Spring Boot with `spring-boot-starter-web`
- Persistence: Spring Data JPA with an embedded file-based H2 database
- Storage location: `./data/bookshelf`
- Endpoints: `GET /books`, `POST /books`
- Components:
  - `BookController` — handles HTTP request/response for both endpoints.
  - `BookRepository` (`JpaRepository<Book, String>`) — persistence, declared
    inline in the controller file.
  - `Book` — JPA entity (`id`, `title`, `author`).

## US-102 — Filter, update, and delete books

Requirements source: `docs/requirements.md` § US-102.

### Why the existing structure needs to change

`BookController` currently talks to `BookRepository` directly and has no
validation or error-handling layer. US-102 adds business rules (required
fields, 255-char max, duplicate detection, not-found handling) that don't
belong in the HTTP layer — they need to be testable in isolation and reused
across `POST`, `PUT`, and `DELETE`. This calls for a service layer and a
centralized exception handler; it does not warrant a new microservice, a
new datastore, or a messaging layer.

### Components and responsibilities

- **`BookController`** (existing, extended)
  - Adds `PUT /books/{id}`, `DELETE /books/{id}`.
  - Adds optional `title`/`author` query params on the existing `GET /books`.
  - Delegates all validation and persistence decisions to `BookService`;
    contains no business logic itself.
  - `PUT /books/{id}` accepts a `Book` body but only reads its `author`
    field — any `title` present in the body is ignored, since title is not
    editable via this endpoint (design review #1).

- **`BookService`** (new)
  - `create(title, author)` — validates required fields and max length
    (255) via `validateField`, checks for a case-insensitive duplicate on
    `title` + `author`, then saves.
  - `findAll(titleFilter, authorFilter)` — builds the filtered query.
    A blank/empty `titleFilter` or `authorFilter` is treated the same as
    an absent one (no filter applied for that field), so `?title=` never
    matches on an empty string (design review #4).
  - `updateAuthor(id, author)` — validates the new `author` via
    `validateField`, loads the existing book or throws not-found, updates
    and saves. Ignores any `title` on the incoming body.
  - `delete(id)` — loads the existing book or throws not-found, then
    deletes.
  - `validateField(name, value)` (private helper) — the single shared
    check for "required, max 255 chars", used by both `create` and
    `updateAuthor` so the two paths can't drift (design review #5).
  - This is the single place validation/duplicate/not-found rules live, so
    each rule can be unit-tested without HTTP.

- **`BookRepository`** (existing interface, extended; moved to its own
  file out of `BookController.java`)
  - Adds derived query methods:
    `existsByTitleIgnoreCaseAndAuthorIgnoreCase`,
    `findByTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase`,
    `findByTitleContainingIgnoreCase`,
    `findByAuthorContainingIgnoreCase`.

- **`BookNotFoundException`, `InvalidBookException`, `DuplicateBookException`**
  (new, unchecked exceptions) — raised by `BookService`, carry the exact
  message text required by `docs/requirements.md`
  (`"no book present"`, `"please check the request payload"`,
  `"record already exist"`).

- **`GlobalExceptionHandler`** (new, `@RestControllerAdvice`)
  - Maps `BookNotFoundException` → `404`, `InvalidBookException` → `400`,
    `DuplicateBookException` → `400`.
  - Returns a small JSON body, e.g. `{"message": "no book present"}`, via a
    new `ErrorResponse` record — keeps error shape consistent across all
    endpoints instead of each controller method building its own response.
  - `message` is the one documented response key for errors (design
    review #6); there is no separate `error` field.

### Data flow

- **Create**: `POST /books` → `BookController` → `BookService.create()` →
  validate → `BookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase`
  → save or throw `DuplicateBookException`/`InvalidBookException` →
  `GlobalExceptionHandler` on failure.
- **Filter**: `GET /books?title=&author=` → `BookController` reads query
  params → `BookService.findAll()` picks the matching repository query →
  JSON array (empty array if no match).
- **Update**: `PUT /books/{id}` → `BookController` → `BookService.updateAuthor()`
  → validate → repository lookup by id (404 if absent) → save → `200`.
- **Delete**: `DELETE /books/{id}` → `BookController` → `BookService.delete()`
  → repository lookup by id (404 if absent) → delete → `204`.

No changes to the storage technology, endpoint base path, or deployment
model are needed for US-102.

### Design review

See `docs/design-review.md` (2026-09-21) for the full list of risks
considered. `Book` continues to be used directly as the request/response
body (no separate DTO) and the check-then-save duplicate check is not
atomic — both accepted as reasonable for this single-user prototype.

