# Design Review

## 2026-09-21 — US-102 (Filter, update, and delete books)

Reviewed: `docs/architecture.md` § US-102, against `docs/requirements.md` § US-102.

| # | Risk / gap found | Decision | Tracked in |
|---|---|---|---|
| 1 | `PUT /books/{id}` request body shape is undefined: since `Book` (the JPA entity) doubles as the request/response DTO, it's unclear whether a `title` sent in the body is silently ignored, overwrites the field, or causes a validation error — requirements say title must not change. | **Fixed** — `BookController`/`BookService` contract now explicitly states any `title` in the `PUT` body is ignored; only `author` is read. | `architecture.md` § Components → `BookController`, `BookService.updateAuthor` |
| 2 | Reusing the `Book` JPA entity directly as the web request/response body (no separate DTO) couples the persistence model to the API contract — e.g. a client could send an `id` in the `POST` body (harmless today since `id` is server-generated, but a latent risk if the entity gains sensitive/internal fields later). | **Accepted** — matches the existing US-101 pattern; introducing a DTO layer is out of scope for this prototype. Revisit if the entity grows fields that shouldn't be client-writable. | Noted here only; no code change |
| 3 | Duplicate check (`existsByTitleIgnoreCaseAndAuthorIgnoreCase`) then `save()` is a check-then-act — two concurrent `POST /books` requests with the same title/author could both pass the check and create duplicates. | **Accepted (deferred)** — non-functional requirements state single-user access is sufficient for this prototype, so concurrent writes are out of scope. Flag for follow-up if multi-user access is ever added. | `docs/requirements.md` non-functional requirements (single-user) |
| 4 | Filter query params (`?title=`, `?author=`) don't define behavior for an explicitly empty string (e.g. `?title=`) vs. the param being absent — could be read as "match books with empty title" instead of "no filter". | **Fixed** — `BookService.findAll` now treats a blank/empty query param the same as an absent one (no filter applied for that field). | `architecture.md` § `BookService.findAll` |
| 5 | Validation rules (required, max length 255) are needed in both `create` and `updateAuthor`; without a single shared check, the two paths could drift (e.g. one enforces 255, the other doesn't). | **Fixed** — both paths now call one private `BookService.validateField(name, value)` helper, avoiding duplicated logic. | `architecture.md` § `BookService` |
| 6 | Error response JSON shape (`{"message": "..."}`) isn't specified in `docs/requirements.md`, only the message text is. Any consumer (or automated test) expecting a different key (e.g. `error`) would break. | **Accepted** — `message` is adopted as the single documented contract; called out explicitly in `architecture.md` so it's the source of truth. | `architecture.md` § `GlobalExceptionHandler` |

### Outcome
No single point of failure or missing requirement coverage found. Six items
reviewed; three required an architecture update (#1, #4, #5), three were
accepted as-is with reasoning recorded above. `docs/architecture.md` has
been updated to reflect the fixes.

