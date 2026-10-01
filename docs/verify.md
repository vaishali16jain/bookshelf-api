# Verification

## 2026-09-22 - SCRUM-2 / US-102

Status: **Complete - ready for Step 8**

Step 7 verified the approved filter, update, delete, create-validation, and
persistence scope. No production code or dependencies were changed. Step 8 was
not performed.

## Verification matrix

| Requirement or approved edge case | Verification | Result |
|---|---|---|
| `GET /books` without filters preserves list behavior | `BookServiceTest.findAllWithNoFiltersReturnsAllBooks`, `BookControllerTest.getAllBooksWithNoFilterReturnsEverything`, functional lifecycle | Pass |
| Title filter is case-insensitive and partial | `BookServiceTest.findAllWithTitleOnlyUsesTitleQuery`, `BookControllerTest.filterBooksByTitleOnly` | Pass |
| Author filter is case-insensitive and partial | `BookServiceTest.findAllWithAuthorOnlyUsesAuthorQuery`, `BookControllerTest.filterBooksByAuthorOnly` | Pass |
| Combined title and author filters use AND | `BookServiceTest.findAllWithBothFiltersUsesCombinedQuery`, `BookControllerTest.filterBooksByTitleAndAuthorUsesAndCondition`, functional lifecycle | Pass |
| No match returns `200` and `[]`; blank filters are absent | `BookControllerTest.filterBooksWithNoMatchReturnsEmptyArray`, `BookControllerTest.emptyFiltersAreTreatedAsAbsent`, corresponding service tests | Pass |
| PUT changes only author and returns `200`; title is ignored | `BookServiceTest.updateAuthorUpdatesExistingBook`, `BookControllerTest.updateBookAuthorReturns200`, `BookControllerTest.updateBookIgnoresTitleInRequestBody`, functional lifecycle | Pass |
| Invalid PUT author (missing, blank, or over 255 characters) returns the exact `400` message | Service validation tests and controller tests `updateBookWithMissingAuthorReturns400`, `updateBookWithBlankAuthorReturns400`, `updateBookWithOversizedAuthorReturns400` | Pass |
| PUT of a missing ID returns the exact `404` message | Service, controller, and functional not-found tests | Pass |
| DELETE returns `204`; a missing ID returns the exact `404` message | Service and controller delete tests, functional lifecycle and not-found test | Pass |
| Valid POST returns `201`; title and author are required and at most 255 characters | Service 255-character boundary test; controller missing, blank, and oversized field tests | Pass |
| Duplicate title and author matching is case-insensitive and returns the exact `400` message | Service duplicate test, case-varied controller duplicate test, functional duplicate test | Pass |
| Malformed JSON uses the approved exact `400` error body | `BookControllerTest.createWithMalformedJsonReturns400` and the global exception-handler mapping | Pass |
| Books persist in local file-backed H2 storage across restart | Packaged app started twice against `target/verify-bookshelf`; the second process returned the same UUID, title, and author created by the first | Pass |
| Single-user access and no authentication | No authentication or multi-user mechanism was added; accepted concurrent duplicate limitation remains unchanged | Pass |

## Test additions

Verification found missing explicit coverage for blank and oversized POST
authors, oversized PUT authors, and the valid 255-character boundary. Added one
service test and three controller integration tests. These tests passed against
the existing production implementation; no feature fix was required.

## Execution evidence

| Command | Result |
|---|---|
| `mvn test "-Dtest=BookServiceTest,BookControllerTest"` | Pass - 39 JUnit tests: 16 service + 23 controller; 0 failures, 0 errors, 0 skipped |
| `mvn test "-Dtest=BookApiFunctionalTest"` | Pass - 7 TestNG tests; 0 failures, 0 errors, 0 skipped |
| `mvn test` | Pass - 46 total tests: 39 JUnit + 7 TestNG; 0 failures, 0 errors, 0 skipped |
| `mvn package -DskipTests` | Pass - executable `target/bookshelf-api-0.1.0.jar` built successfully |
| `& 'C:\Program Files\Java\jdk-21\bin\java.exe' -jar target/bookshelf-api-0.1.0.jar --server.port=18082 '--spring.datasource.url=jdbc:h2:file:./target/verify-bookshelf;DB_CLOSE_ON_EXIT=FALSE'` (first process) | Pass - application started on port 18082; POST created UUID `025a9ee0-4771-4ff5-b0e0-750dbfd2a189` |
| Same Java command after stopping the first process | Pass - GET returned UUID `025a9ee0-4771-4ff5-b0e0-750dbfd2a189`, title `Persistence Check 2026-09-22`, author `Verify Agent`; DELETE cleanup followed by filtered GET returned `200` with `[]` |

The test runs used Maven's Java 21.0.9 runtime while compiling for Java 17.
An initial direct `java -jar` attempt resolved the shell's Java 8 executable
and failed with `UnsupportedClassVersionError`; `mvn -version` identified
`C:\Program Files\Java\jdk-21`, and the persistence check passed when rerun
with that explicit executable. This PATH mismatch is environmental and does not
affect the Maven verification result.

Non-failing warnings were emitted for dynamic JVM agent loading, Spring's test
context `open-in-view` default, and Selenium's missing Chrome 153 CDP binding.
The browser flow itself passed.

## Document and architecture quality

| Check | Result |
|---|---|
| Requirements against implementation and tests | Pass - every US-102 acceptance criterion and documented edge case maps to a check above |
| Architecture routes | Pass - controller exposes GET/POST `/books` and PUT/DELETE `/books/{id}` exactly as documented |
| Architecture components and data flow | Pass - controller delegates to `BookService`; repository derived queries, domain exceptions, handler, and file-backed H2 configuration match the design |
| Design-review decisions | Pass - ignored PUT title, blank-filter behavior, shared validation, malformed JSON handling, and `message` error shape are implemented; accepted DTO and concurrency limitations remain scoped out |
| Implementation plan and implementation report | Pass - tasks #1-#16 are present and marked complete; their recorded Step 5 counts remain clearly identified as historical Step 5 evidence |
| Code review | Pass - Step 6 reports no blocking findings and all seven checklist categories pass; the non-blocking test-indentation note remains cosmetic |
| Evidence quality | Pass - this report records commands and observed counts from Step 7 execution, distinguishes prior-phase counts, and includes the restart check's observed record identity |

## Changed files in Step 7

- `src/test/java/com/example/bookshelf/service/BookServiceTest.java`
- `src/test/java/com/example/bookshelf/controller/BookControllerTest.java`
- `docs/verify.md`

## Final result

**Pass.** US-102 is verified with comprehensive unit, integration, functional,
persistence, and document-quality evidence. There are no Step 7 blockers.
Accepted DTO coupling and non-atomic duplicate enforcement remain intentionally
out of scope. Step 8 has not been performed.