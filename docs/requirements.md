
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

