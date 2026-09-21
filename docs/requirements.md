
# Requirements

## US-101 — Add and view books

As a reader, I want to add a book and view my bookshelf via API, so that I can keep track of what I own.

### Acceptance criteria
- `POST /books` creates a book with `title` and `author`
- `GET /books` returns all books
- Responses use JSON

### Non-functional requirements
- Books must be persisted locally and remain available after an application restart.
- The prototype may use an embedded file-based database; no external database server is required.
- Single-user access is sufficient; authentication is not required at this stage.

## US-102 — Filter, update, and delete books

_JIRA reference: [SCRUM-1](https://epam-testing.atlassian.net/browse/SCRUM-1)_

As a reader, I want to filter my bookshelf, update a book's author, and
remove a book, so that I can keep my bookshelf accurate and find books
easily.

### Acceptance criteria

**Filter books**
- `GET /books` with no query params returns all books, unchanged from US-101.
- `GET /books?title={value}` returns only books whose `title` contains
  `{value}` (case-insensitive, partial match).
- `GET /books?author={value}` returns only books whose `author` contains
  `{value}` (case-insensitive, partial match).
- `GET /books?title={t}&author={a}` combines both filters (AND condition).
- If no books match the filter, respond `200 OK` with an empty JSON array `[]`.

**Update a book**
- `PUT /books/{id}` updates the `author` field of the book identified by
  `{id}` and returns `200 OK` with the updated book.
- `title` is not editable via this endpoint; only `author` may change.
- If `{id}` does not exist, respond `404 Not Found` with message
  `"no book present"`.
- If the request payload is missing/invalid (e.g. `author` blank or exceeds
  255 characters), respond `400 Bad Request` with message
  `"please check the request payload"`.

**Delete a book**
- `DELETE /books/{id}` deletes the book identified by `{id}` and returns
  `204 No Content`.
- If `{id}` does not exist, respond `404 Not Found` with message
  `"no book present"`.

**Create validation (extends US-101 `POST /books`)**
- `title` and `author` are required; max length 255 characters each.
- If `title` or `author` is missing, blank, or exceeds 255 characters,
  respond `400 Bad Request` with message `"please check the request payload"`.
- A duplicate is a book with the same `title` and `author`
  (case-insensitive match on both). Creating a duplicate responds
  `400 Bad Request` with message `"record already exist"`.

### Non-functional requirements
- Same as US-101: books are persisted locally (embedded file-based
  database), single-user access, no authentication required.

