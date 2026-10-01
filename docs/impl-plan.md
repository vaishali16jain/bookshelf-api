# Implementation Plan

## US-102 tasks — Filter, update, and delete books

Source: `docs/architecture.md` § US-102, `docs/design-review.md` (2026-09-22, human-approved).

| # | Task | Depends on | Blocked? |
|---|---|---|---|
| 1 | Move `BookRepository` out of `BookController.java` into its own file (`repository/BookRepository.java`), no behavior change. | [no deps] | No |
| 2 | Add derived query methods to `BookRepository`: `existsByTitleIgnoreCaseAndAuthorIgnoreCase`, `findByTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase`, `findByTitleContainingIgnoreCase`, `findByAuthorContainingIgnoreCase`. | #1 | Yes — needs the repository in its own file first |
| 3 | Create exception classes `BookNotFoundException`, `InvalidBookException`, `DuplicateBookException` (unchecked, each carrying the exact required message text). | [no deps] | No |
| 4 | Create `ErrorResponse` record (single `message` field) for the error JSON body. | [no deps] | No |
| 5 | Create `GlobalExceptionHandler` (`@RestControllerAdvice`) mapping each exception from #3 to its HTTP status and mapping `HttpMessageNotReadableException` to `400`, using `ErrorResponse` (#4) as the body. | #3, #4 | Yes — needs exceptions and `ErrorResponse` to exist |
| 6 | Create `BookService` with a private `validateField(value)` shared helper (required + max 255 chars). | #3 | Yes — throws `InvalidBookException` from #3 |
| 7 | Implement `BookService.create(title, author)`: call `validateField` (#6), check duplicate via `existsByTitleIgnoreCaseAndAuthorIgnoreCase` (#2), throw `DuplicateBookException` (#3) or save. | #2, #3, #6 | Yes |
| 8 | Implement `BookService.findAll(titleFilter, authorFilter)`: treat blank/absent params as "no filter", pick the matching repository query (#2). | #2 | Yes |
| 9 | Implement `BookService.updateAuthor(id, author)`: call `validateField` (#6), load by id or throw `BookNotFoundException` (#3), update, save. | #3, #6 | Yes |
| 10 | Implement `BookService.delete(id)`: load by id or throw `BookNotFoundException` (#3), delete. | #3 | Yes |
| 11 | Update `BookController`: delegate `POST /books` to `BookService.create` (#7); remove direct `BookRepository` calls. | #7 | Yes |
| 12 | Update `BookController`: add optional `title`/`author` query params to `GET /books`, delegate to `BookService.findAll` (#8). | #8 | Yes |
| 13 | Add `PUT /books/{id}` to `BookController`, reading only `author` from the body (ignore any `title`), delegate to `BookService.updateAuthor` (#9). | #9 | Yes |
| 14 | Add `DELETE /books/{id}` to `BookController` returning `204`, delegate to `BookService.delete` (#10). | #10 | Yes |
| 15 | Unit tests for `BookService`: shared required/max-length validation on create and update, case-insensitive duplicate detection, not-found update/delete, and filter combinations (none/title/author/both/blank). | #6, #7, #8, #9, #10 | Yes — exercises all service behavior |
| 16 | Integration tests for `BookController` + `GlobalExceptionHandler`: existing create/list behavior; filter/update/delete happy paths and statuses; PUT title ignored; empty filters treated as absent; duplicate, invalid, not-found, and malformed-JSON failures with exact status and `{"message":"..."}` body. | #5, #11, #12, #13, #14, #15 | Yes — exercises the completed service and HTTP stack |

### Step 5 implementation status

All tasks #1-#16 are complete as of 2026-09-22. The dependency and blocked
columns above describe the approved build sequence; no implementation blocker
remains. See `docs/implementation.md` for the implementation details and test
evidence.

The approved Step 6 review rework for task #16 is complete as of 2026-09-22.
Focused controller tests now cover POST requests missing `title`, POST requests
missing `author`, and PUT requests missing `author`; each verifies `400 Bad
Request` and the exact required error message. The focused controller suite
passes 20 tests, and the full suite passes 35 JUnit tests plus 7 TestNG tests.

### Dependency order (build sequence)

1. **Foundational (parallelizable):** #1 → #2, #3, #4 — no interdependencies among #2/#3/#4 themselves.
2. **Error handling:** #5 (needs #3, #4).
3. **Service layer:** #6 → #7, #8, #9, #10 (each service method only needs #6 plus its own repository query from #2).
4. **Controller layer:** #11, #12, #13, #14 (each wired to its corresponding service method; independent of each other).
5. **Tests last:** #15 (service-level), then #16 (full-stack, needs #5 + all controller endpoints and follows #15).

### Blocked tasks summary

- All tasks except #1, #3, #4 are blocked on at least one earlier task completing — see the "Depends on" column above.
- #16 is the most blocked task: it cannot start until the exception handler (#5), all four controller endpoint tasks (#11–#14), and service tests (#15) are done.

### Validation sequence

1. Run focused `BookServiceTest` tests after #15; all create, filter, update, delete, validation, duplicate, and not-found cases must pass.
2. Run focused `BookControllerTest` tests after #16; verify exact HTTP statuses and error response bodies, including malformed JSON and ignored PUT titles.
3. Run `BookApiFunctionalTest` to confirm the user-visible create/list/filter/update/delete workflow and preserve US-101 behavior.
4. Run the full Maven test suite as the final Step 5 completion check.

### Planning blockers and approval gate

- No unresolved design or implementation-planning blocker remains for US-102.
- Accepted design-review risks remain intentionally out of scope: separate request/response DTOs and atomic duplicate enforcement for concurrent writers.
- Step 5 must not begin until a human explicitly approves this implementation plan.

