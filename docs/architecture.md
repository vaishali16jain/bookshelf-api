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

### US-102 design details

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
    `validateField` before resolving the existing book or throwing not-found,
    then updates and saves. Ignores any `title` on the incoming body. If both
    author validation and ID lookup fail, validation takes precedence and the
    response is `400 Bad Request` with `"please check the request payload"`
    (approved design decision; see `docs/design-review.md`).
  - `delete(id)` — loads the existing book or throws not-found, then
    deletes.
  - `validateField(value)` (private helper) — the single shared
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
  - Maps Spring's `HttpMessageNotReadableException` → `400` with
    `"please check the request payload"`, preserving the same contract for
    malformed or unreadable JSON that cannot reach `BookService` validation.
  - Returns a small JSON body, e.g. `{"message": "no book present"}`, via a
    new `ErrorResponse` record — keeps error shape consistent across all
    endpoints instead of each controller method building its own response.
  - `message` is the one documented response key for errors (design
    review #6); there is no separate `error` field.

### US-102 operational flow

- **Create**: validate the required fields and lengths, check for a
  case-insensitive title-and-author duplicate, then save. The exception
  handler maps invalid and duplicate outcomes to their specified `400`
  responses.
- **Filter**: pass optional title and author parameters to the service, which
  selects a repository query; return matching books as a JSON array, including
  `[]` when none match.
- **Update**: validate the requested author before resolving the book by ID,
  update only the author, then save. Invalid-payload and not-found outcomes use
  their specified responses. If both conditions apply, author validation
  takes precedence and returns `400 Bad Request` with
  `"please check the request payload"` (approved design decision).
- **Delete**: resolve the book by ID, delete it, and return `204`; a missing
  book uses the specified not-found response.

No changes to the storage technology, endpoint base path, or deployment
model are needed for US-102.

### Design review

See `docs/design-review.md` (2026-09-22) for the full list of risks
considered. `Book` continues to be used directly as the request/response
body (no separate DTO) and the check-then-save duplicate check is not
atomic — both accepted as reasonable for this single-user prototype.

Requirements source: `docs/requirements.md` § US-102.

### US-102 Step 2 traceability notes

- **`BookController`** — retain the existing `GET /books` and `POST /books`
  routes; accept optional title and author filters on GET and add
  `PUT /books/{id}` and `DELETE /books/{id}`. Keep HTTP binding and response
  handling here, delegating business rules to `BookService`.
- **`BookService`** — own case-insensitive partial filtering, create-field
  validation, duplicate detection, author update, and not-found decisions.
  Validate `title` and `author` as required, nonblank values of at most 255
  characters on create; validate the update's author with the same limits.
  Use one shared field-validation rule where practical.
- **`BookRepository`** — preserve the existing persistence boundary and add
  or use query operations for case-insensitive contains matching, combined
  with AND when both filters are supplied, and case-insensitive duplicate
  lookup on title and author. Keep the existing embedded database and
  single-user deployment model.
- **Exception handling** — translate not-found and invalid/duplicate outcomes
  into the specified HTTP statuses and message text. The requirements do
  not prescribe an error JSON object shape; the 2026-09-22 review retains
  `{"message": "..."}` as a design decision.

### Acceptance-criteria flow

- **Filter**: `GET /books` binds optional query parameters, delegates to the
  service, and returns the matching books as JSON. No parameters returns all
  books; supplied title/author filters use case-insensitive partial matching,
  and both filters are combined with AND. No matches returns `200` and `[]`.
- **Update**: `PUT /books/{id}` passes the identifier and requested author to
  the service. The service validates the author and resolves the book, updates
  only its author, and returns the updated book with `200`; absent identifiers
  map to `404` with `"no book present"`. If both the ID and author are
  invalid, author validation takes precedence and returns `400 Bad Request`
  with `"please check the request payload"` (approved design decision).
- **Delete**: `DELETE /books/{id}` asks the service to find and delete the
  book; success returns `204`, and an absent identifier maps to `404` with
  `"no book present"`.
- **Create validation extension**: existing `POST /books` validation rejects
  missing, blank, or over-255-character title/author values with `400` and
  `"please check the request payload"`; a case-insensitive title-and-author
  duplicate returns `400` and `"record already exist"`.

### Step 3 review status

The 2026-09-22 review already recorded decisions for the items below. They
remain design choices, not additional requirements, and are retained for this
proposal: ignore any `title` in a PUT body; treat blank filters as absent; use
`{"message": "..."}` for error responses; and map malformed/unreadable JSON
to the invalid-payload response. See `docs/design-review.md` for the dated
decision history.

The combined PUT error case is resolved by an approved design decision: when
the ID is nonexistent and the author is invalid, author validation occurs
before ID lookup, so the response is `400 Bad Request` with
`"please check the request payload"`. This precedence is a design decision,
not an addition to `docs/requirements.md`; see `docs/design-review.md`.

### Testability and risks

Keep matching, validation, duplicate checks, and update/delete decisions in
`BookService` so they can be tested independently of HTTP. Controller-level
tests should verify route statuses, JSON results, required error messages,
and retained design decisions, including that the combined PUT error case
returns `400 Bad Request` with `"please check the request payload"`.
Repository tests or focused service tests should cover case-insensitive
partial matching and AND combination. No persistence or deployment change is
required.
