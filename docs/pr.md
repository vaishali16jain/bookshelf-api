# Pull Request

## Summary

Complete the approved US-102 bookshelf API work and remediate the resolved dependency graph while preserving the Java 17 compiler target. The changes keep the approved routes and behavior, restore the existing test suites under the updated Spring/Jackson stack, and pass the latest Step 7 verification.

## Changes Made

- Updated `pom.xml` to Spring Boot 4.0.8; aligned Jackson BOMs to 3.1.7/2.21.7, JUnit Platform to 6.0.3, and Surefire providers to 3.5.6; updated test-scoped RestAssured to 6.0.0 for Groovy 5 compatibility; retained Java 17.
- Updated `src/main/java/com/example/bookshelf/BookshelfApplication.java`, `src/main/java/com/example/bookshelf/controller/BookController.java`, `src/main/java/com/example/bookshelf/exception/BookNotFoundException.java`, `src/main/java/com/example/bookshelf/exception/DuplicateBookException.java`, `src/main/java/com/example/bookshelf/exception/ErrorResponse.java`, `src/main/java/com/example/bookshelf/exception/GlobalExceptionHandler.java`, `src/main/java/com/example/bookshelf/exception/InvalidBookException.java`, `src/main/java/com/example/bookshelf/model/Book.java`, `src/main/java/com/example/bookshelf/repository/BookRepository.java`, and `src/main/java/com/example/bookshelf/service/BookService.java`; the Java diff is formatting/import normalization, with no production behavior change found.
- Updated `src/test/java/com/example/bookshelf/automation/BookApiFunctionalTest.java`, `src/test/java/com/example/bookshelf/controller/BookControllerTest.java`, and `src/test/java/com/example/bookshelf/service/BookServiceTest.java` for the current test stack and approved coverage; the controller suite includes the invalid-author/nonexistent-ID precedence regression.
- Updated `docs/architecture.md`, `docs/design-review.md`, `docs/impl-plan.md`, `docs/implementation.md`, `docs/review.md`, and `docs/verify.md` with the approved design, dependency remediation, review, and verification history. Detailed verification is in [docs/verify.md](verify.md); review findings are in [docs/review.md](review.md).
- Updated [CHANGELOG.md](../CHANGELOG.md) with the Unreleased dependency/test compatibility changes and current verification evidence; added this PR description as `docs/pr.md`.

## Test Evidence

Current Step 7 evidence is recorded in [docs/verify.md](verify.md). Focused suite results from the current Surefire reports:

```text
BookServiceTest: Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
BookControllerTest: Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
BookApiFunctionalTest: Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```

Current execution results:

```text
mvn test
PASS — 40 JUnit tests and 7 TestNG tests, 0 failures, 0 errors, 0 skipped.

mvn -DskipTests package
PASS — executable target/bookshelf-api-0.1.0.jar built.

mvn help:evaluate "-Dexpression=maven.compiler.release" -q -DforceStdout
PASS — 17.
```

Packaged application smoke on port 18084 with isolated in-memory H2:

```text
Initial GET /books: HTTP 200, []
POST /books: HTTP 201, response contained a generated ID.
Follow-up GET /books: HTTP 200, contained the created record.
DELETE cleanup: HTTP 204.
Final GET /books: HTTP 200, [].
```

Fresh OSV batch audit result from `target/osv-step7-fresh.json`:

```json
{
  "queriedAt": "2026-10-08T06:56:55.6977218Z",
  "source": "https://api.osv.dev/v1/querybatch",
  "resolvedCoordinateCount": 176,
  "productionScopeCoordinateCount": 75,
  "testScopeCoordinateCount": 101,
  "matchingCoordinateCount": 0,
  "advisoryRecordCount": 0,
  "uniqueAdvisoryIdCount": 0
}
```

The current full-suite run substantiates review finding LOW-2: the final controller Surefire report records 24 tests with 0 failures/errors/skips, and its XML contains both the invalid-author/missing-ID `400` and valid-author/missing-ID `404` cases. This supersedes the earlier zero-test artifact from the isolated multi-provider run. The `docs/review.md` artifact still ends with the earlier “NEEDS CONFIRMATION” wording; the newer resolution is recorded in `docs/verify.md` and is not silently attributed to an edit of the review file.

Non-failing warnings: Mockito/Byte Buddy dynamic-agent loading, Spring test-context `open-in-view`, and Selenium's missing CDP implementation for Chrome 154. The functional tests passed despite these warnings.

## Known Limitations

- `Book` remains coupled as both persistence entity and API request/response model, as accepted in the design review.
- Duplicate detection remains a non-atomic check-then-save operation; concurrent duplicate requests are outside the approved single-user scope.
- The smoke test used isolated in-memory H2 and did not repeat the process-restart persistence check in this Step 7 run.
- The OSV audit covers the resolved default project dependency graph and compile/runtime/test scopes only. Maven build plugins, alternate profiles, and platform-specific graphs were excluded; a zero match is not an application-specific exploitability assessment or a guarantee of future advisory status.
- The current `docs/review.md` still contains the historical LOW-2 “NEEDS CONFIRMATION” outcome, although the latest full-suite evidence resolves that finding in `docs/verify.md`.

## Reviewer Checklist

- [ ] Correctness: confirm US-102 routes, filtering, update/delete behavior, and approved validation-before-lookup precedence.
- [ ] Security: review input handling, accepted entity/API coupling, and the single-user threat assumptions.
- [ ] Error handling: confirm required 400/404/204 statuses and message bodies, including malformed JSON.
- [ ] Test coverage: confirm focused and full-suite results and the LOW-2 resolution documented in `docs/verify.md`.
- [ ] Clarity: review the compatibility changes and confirm Java formatting/import changes contain no unintended behavior changes.
- [ ] DRY: confirm shared service validation remains centralized.
- [ ] Dependency safety: review the zero-match OSV result and its stated graph/scope limitations.
