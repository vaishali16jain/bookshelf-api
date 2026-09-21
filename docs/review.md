# Code Review

## 2026-09-21 — US-102 (Filter, update, and delete books)

Reviewed implementation: `BookController`, `BookService`, `BookRepository`,
`GlobalExceptionHandler`, exception classes, and all tests (JUnit + TestNG),
against `docs/requirements.md` § US-102.

| Area | Finding | Verdict |
|---|---|---|
| Correctness | All US-102 acceptance criteria implemented as specified: filter (title/author/both/blank-as-absent), `PUT` updates only `author` (title silently ignored, matches spec), `DELETE` → 204, both 404 with `"no book present"`. Create validation + case-insensitive duplicate detection match exactly. | Pass |
| Security | No secrets/credentials in code or `application.properties`. SQL injection not possible — all repository methods are parameterized JPA derived queries. `Book` entity is reused as the request DTO, but the controller reads only `title`/`author` explicitly, so there is no mass-assignment of `id`. Front-end already escapes rendered output (`escapeHtml` in `app.js`). | Pass (entity-as-DTO trade-off already logged in `design-review.md` #2) |
| Error Handling | `GlobalExceptionHandler` correctly maps the 3 business exceptions to 404/400. **Gap found:** malformed JSON body (e.g. broken syntax) threw `HttpMessageNotReadableException`, which was unhandled and fell back to Spring Boot's default error body, breaking the documented `{"message": "..."}` contract. | Fixed — added `@ExceptionHandler(HttpMessageNotReadableException.class)` returning `400` with `"please check the request payload"` |
| Test Coverage | Happy paths (create/filter/update/delete) and not-found/duplicate/blank-field edge cases covered at unit (`BookServiceTest`) and integration level (`BookControllerTest`, `BookApiFunctionalTest`). **Gap found:** no test for malformed JSON. | Fixed — added `createWithMalformedJsonReturns400` to `BookControllerTest` |
| Code Clarity | Method names (`create`, `findAll`, `updateAuthor`, `validateField`) are self-explanatory; logic reads linearly without needing comments. | Pass |
| DRY | `validateField` is already shared between `create`/`updateAuthor` (per `design-review.md` #5). The 3 handlers in `GlobalExceptionHandler` repeat a similar one-line pattern — too small to be worth abstracting further. | Pass — no action needed |
| Dependency Safety | `spring-boot-starter-parent` pinned at `3.3.0` (May 2024); later 3.3.x patch releases exist with bundled security fixes — worth bumping. Test-only additions (`selenium-java`, `rest-assured`, `testng`, `webdrivermanager`) don't ship in the production jar, limiting runtime exposure. No verified critical CVE found via manual review; a live scan (OWASP Dependency-Check / Dependabot) is recommended before merging since this review has no access to a CVE database. | Needs follow-up (non-blocking) |

### Outcome

Two real gaps found (both under Error Handling / Test Coverage, same root
cause): malformed JSON requests bypassed the documented error contract.
Both fixed in `GlobalExceptionHandler` and `BookControllerTest`. All other
areas pass. Full suite re-run after the fix: 24 JUnit tests + 7 TestNG
tests, all green.

Outstanding, non-blocking follow-up: bump `spring-boot-starter-parent` to
the latest `3.3.x` patch and run an automated dependency vulnerability scan
before merging to `main`.

## 2026-09-21 — Verification (Step 7)

### Code verification — acceptance criteria vs. tests

Every acceptance criterion and edge case in `docs/requirements.md` § US-102
now has a corresponding test. Newly added during this verification pass:
`getAllBooksWithNoFilterReturnsEverything`, `filterBooksByAuthorOnly`,
`updateBookIgnoresTitleInRequestBody`, `updateBookWithBlankAuthorReturns400`,
`createBookWithOversizedTitleReturns400` (all in `BookControllerTest`).

| Requirement | Test | Result |
|---|---|---|
| `GET /books` with no params returns all books | `BookControllerTest.getAllBooksWithNoFilterReturnsEverything` | Pass |
| `GET ?title=` partial, case-insensitive | `BookControllerTest.filterBooksByTitleAndAuthor` | Pass |
| `GET ?author=` partial, case-insensitive | `BookControllerTest.filterBooksByAuthorOnly` | Pass |
| `GET ?title=&author=` combined (AND) | `BookApiFunctionalTest.fullLifecycle_createFilterUpdateDelete` (REST Assured) | Pass |
| No match → `200` `[]` | `BookControllerTest.filterBooksWithNoMatchReturnsEmptyArray` | Pass |
| `PUT` updates `author`, returns `200` | `BookControllerTest.updateBookAuthorReturns200` | Pass |
| `PUT` ignores `title` in body | `BookControllerTest.updateBookIgnoresTitleInRequestBody` | Pass |
| `PUT` missing id → `404` `"no book present"` | `BookControllerTest.updateMissingBookReturns404` | Pass |
| `PUT` blank/invalid `author` → `400` | `BookControllerTest.updateBookWithBlankAuthorReturns400` | Pass |
| `DELETE` → `204` | `BookControllerTest.deleteBookReturns204` | Pass |
| `DELETE` missing id → `404` `"no book present"` | `BookControllerTest.deleteMissingBookReturns404` | Pass |
| `POST` required/max-255 validation → `400` | `BookControllerTest.createInvalidBookReturns400`, `createBookWithOversizedTitleReturns400` | Pass |
| `POST` duplicate (case-insensitive) → `400` `"record already exist"` | `BookControllerTest.createDuplicateBookReturns400` | Pass |
| Malformed JSON body → `400` (contract consistency) | `BookControllerTest.createWithMalformedJsonReturns400` | Pass |

Full suite executed via `mvn test`: **36 tests, 0 failures, 0 errors**
(29 JUnit — `BookControllerTest` x15, `BookServiceTest` x14 — plus 7 TestNG
in `BookApiFunctionalTest`, including the Selenium UI test).

### Architecture cross-check (`docs/architecture.md` vs. code)

| Item | Architecture doc says | Code reality | Result |
|---|---|---|---|
| Routes | `GET/POST/PUT/DELETE /books[, /{id}]` | Matches exactly in `BookController` | Pass |
| `BookService.validateField` | Documented as `validateField(name, value)` (two params) | Actual signature is `validateField(String value)` — single param, no field name | **Fail** — doc/code drift, cosmetic only (behavior is correct) |
| `GlobalExceptionHandler` | Documents 3 handlers (`BookNotFoundException`, `InvalidBookException`, `DuplicateBookException`) | Has a 4th handler, `HttpMessageNotReadableException`, added during the code review fix | **Fail** — doc is stale, missing the malformed-JSON handler added after `architecture.md` was last written |

### Documentation content-quality check

| Document | Check | Result |
|---|---|---|
| `docs/requirements.md` | Every acceptance criterion is concrete/testable (no vague terms); JIRA link resolves to `SCRUM-2` | Pass |
| `docs/architecture.md` | Component list vs. code | **Fail** — 2 stale items (see cross-check above) |
| `docs/design-review.md` | All 6 findings have a recorded decision and are reflected in the code | Pass |
| `docs/impl-plan.md` | Task #6 references `validateField(name, value)` — same drift as architecture.md | **Fail** — same root cause |
| `docs/review.md` (this file) | Reflects the actual fix applied (`HttpMessageNotReadableException` handler) | Pass |

### Outcome

Code verification: **all pass** (36/36 tests green, every requirement traced
to a test). Documentation verification: **2 stale references found** —
`docs/architecture.md` and `docs/impl-plan.md` both describe
`validateField` with a `name` parameter that doesn't exist in code, and
`docs/architecture.md`'s `GlobalExceptionHandler` section is missing the
`HttpMessageNotReadableException` handler added during the Step 6 review.
Per the QA role, these are reported here for the human/implementation step
to correct, not fixed in this pass.

