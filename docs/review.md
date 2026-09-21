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
