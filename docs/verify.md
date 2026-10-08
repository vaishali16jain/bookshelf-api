# Verification

## 2026-09-22 - SCRUM-2 / US-102

Status: **Complete - ready for Step 8**

Step 7 verified the approved filter, update, delete, create-validation, and
persistence scope. No production code or dependencies were changed. Step 8 was
not performed.

## Verification matrix

| Requirement or approved edge case                                                           | Verification                                                                                                                                                                       | Result |
| ------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------ |
| `GET /books` without filters preserves list behavior                                        | `BookServiceTest.findAllWithNoFiltersReturnsAllBooks`, `BookControllerTest.getAllBooksWithNoFilterReturnsEverything`, functional lifecycle                                         | Pass   |
| Title filter is case-insensitive and partial                                                | `BookServiceTest.findAllWithTitleOnlyUsesTitleQuery`, `BookControllerTest.filterBooksByTitleOnly`                                                                                  | Pass   |
| Author filter is case-insensitive and partial                                               | `BookServiceTest.findAllWithAuthorOnlyUsesAuthorQuery`, `BookControllerTest.filterBooksByAuthorOnly`                                                                               | Pass   |
| Combined title and author filters use AND                                                   | `BookServiceTest.findAllWithBothFiltersUsesCombinedQuery`, `BookControllerTest.filterBooksByTitleAndAuthorUsesAndCondition`, functional lifecycle                                  | Pass   |
| No match returns `200` and `[]`; blank filters are absent                                   | `BookControllerTest.filterBooksWithNoMatchReturnsEmptyArray`, `BookControllerTest.emptyFiltersAreTreatedAsAbsent`, corresponding service tests                                     | Pass   |
| PUT changes only author and returns `200`; title is ignored                                 | `BookServiceTest.updateAuthorUpdatesExistingBook`, `BookControllerTest.updateBookAuthorReturns200`, `BookControllerTest.updateBookIgnoresTitleInRequestBody`, functional lifecycle | Pass   |
| Invalid PUT author (missing, blank, or over 255 characters) returns the exact `400` message | Service validation tests and controller tests `updateBookWithMissingAuthorReturns400`, `updateBookWithBlankAuthorReturns400`, `updateBookWithOversizedAuthorReturns400`            | Pass   |
| PUT of a missing ID returns the exact `404` message                                         | Service, controller, and functional not-found tests                                                                                                                                | Pass   |
| DELETE returns `204`; a missing ID returns the exact `404` message                          | Service and controller delete tests, functional lifecycle and not-found test                                                                                                       | Pass   |
| Valid POST returns `201`; title and author are required and at most 255 characters          | Service 255-character boundary test; controller missing, blank, and oversized field tests                                                                                          | Pass   |
| Duplicate title and author matching is case-insensitive and returns the exact `400` message | Service duplicate test, case-varied controller duplicate test, functional duplicate test                                                                                           | Pass   |
| Malformed JSON uses the approved exact `400` error body                                     | `BookControllerTest.createWithMalformedJsonReturns400` and the global exception-handler mapping                                                                                    | Pass   |
| Books persist in local file-backed H2 storage across restart                                | Packaged app started twice against `target/verify-bookshelf`; the second process returned the same UUID, title, and author created by the first                                    | Pass   |
| Single-user access and no authentication                                                    | No authentication or multi-user mechanism was added; accepted concurrent duplicate limitation remains unchanged                                                                    | Pass   |

## Test additions

Verification found missing explicit coverage for blank and oversized POST
authors, oversized PUT authors, and the valid 255-character boundary. Added one
service test and three controller integration tests. These tests passed against
the existing production implementation; no feature fix was required.

## Execution evidence

| Command                                                                                                                                                                                                         | Result                                                                                                                                                                                         |
| --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `mvn test "-Dtest=BookServiceTest,BookControllerTest"`                                                                                                                                                          | Pass - 39 JUnit tests: 16 service + 23 controller; 0 failures, 0 errors, 0 skipped                                                                                                             |
| `mvn test "-Dtest=BookApiFunctionalTest"`                                                                                                                                                                       | Pass - 7 TestNG tests; 0 failures, 0 errors, 0 skipped                                                                                                                                         |
| `mvn test`                                                                                                                                                                                                      | Pass - 46 total tests: 39 JUnit + 7 TestNG; 0 failures, 0 errors, 0 skipped                                                                                                                    |
| `mvn package -DskipTests`                                                                                                                                                                                       | Pass - executable `target/bookshelf-api-0.1.0.jar` built successfully                                                                                                                          |
| `& 'C:\Program Files\Java\jdk-21\bin\java.exe' -jar target/bookshelf-api-0.1.0.jar --server.port=18082 '--spring.datasource.url=jdbc:h2:file:./target/verify-bookshelf;DB_CLOSE_ON_EXIT=FALSE'` (first process) | Pass - application started on port 18082; POST created UUID `025a9ee0-4771-4ff5-b0e0-750dbfd2a189`                                                                                             |
| Same Java command after stopping the first process                                                                                                                                                              | Pass - GET returned UUID `025a9ee0-4771-4ff5-b0e0-750dbfd2a189`, title `Persistence Check 2026-09-22`, author `Verify Agent`; DELETE cleanup followed by filtered GET returned `200` with `[]` |

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

| Check                                         | Result                                                                                                                                                                                             |
| --------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Requirements against implementation and tests | Pass - every US-102 acceptance criterion and documented edge case maps to a check above                                                                                                            |
| Architecture routes                           | Pass - controller exposes GET/POST `/books` and PUT/DELETE `/books/{id}` exactly as documented                                                                                                     |
| Architecture components and data flow         | Pass - controller delegates to `BookService`; repository derived queries, domain exceptions, handler, and file-backed H2 configuration match the design                                            |
| Design-review decisions                       | Pass - ignored PUT title, blank-filter behavior, shared validation, malformed JSON handling, and `message` error shape are implemented; accepted DTO and concurrency limitations remain scoped out |
| Implementation plan and implementation report | Pass - tasks #1-#16 are present and marked complete; their recorded Step 5 counts remain clearly identified as historical Step 5 evidence                                                          |
| Code review                                   | Pass - Step 6 reports no blocking findings and all seven checklist categories pass; the non-blocking test-indentation note remains cosmetic                                                        |
| Evidence quality                              | Pass - this report records commands and observed counts from Step 7 execution, distinguishes prior-phase counts, and includes the restart check's observed record identity                         |

## Changed files in Step 7

- `src/test/java/com/example/bookshelf/service/BookServiceTest.java`
- `src/test/java/com/example/bookshelf/controller/BookControllerTest.java`
- `docs/verify.md`

## Final result

**Pass.** US-102 is verified with comprehensive unit, integration, functional,
persistence, and document-quality evidence. There are no Step 7 blockers.
Accepted DTO coupling and non-atomic duplicate enforcement remain intentionally
out of scope. Step 8 has not been performed.

## 2026-10-08 - US-102 Step 7 Verification

**Result: Code PASS; docs FAIL (LOW-1 attribution remains partly unresolved);
dependency safety FAIL (OSV advisories matched).** Step 8 is not ready. No
feature fix or missing test was identified, and no tests or production files
were changed during this verification.

### Criterion-to-check matrix

| Requirement or approved decision                                                                                                                       | Current check and evidence                                                                                                                                                                                                                                                                       | Result |
| ------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------ |
| Combined `PUT` failure: invalid author plus missing ID returns `400` and the exact invalid-payload message; valid author plus missing ID remains `404` | `BookControllerTest.updateNonexistentBookWithBlankAuthorReturns400` and `updateMissingBookReturns404`; both included in the 24-test controller run                                                                                                                                               | Pass   |
| Create requires title and author; missing, blank, or over-255-character values return `400` and the exact message                                      | `BookControllerTest.createBookWithMissingTitleReturns400`, `createBookWithMissingAuthorReturns400`, `createInvalidBookReturns400`, `createBookWithBlankAuthorReturns400`, `createBookWithOversizedTitleReturns400`, `createBookWithOversizedAuthorReturns400`; service validation tests also ran | Pass   |
| Duplicate create uses case-insensitive title and author matching and returns `400` with `record already exist`                                         | `BookServiceTest.createThrowsDuplicateBookExceptionWhenAlreadyExists`, `BookControllerTest.createDuplicateBookReturns400`, and `BookApiFunctionalTest.creatingDuplicateBookReturns400WithMessage`                                                                                                | Pass   |
| Title-only and author-only filters are partial and case-insensitive; both filters combine with AND                                                     | Service repository-selection tests and controller `filterBooksByTitleOnly`, `filterBooksByAuthorOnly`, and `filterBooksByTitleAndAuthorUsesAndCondition`; functional lifecycle also exercises combined filtering                                                                                 | Pass   |
| Blank filters act as absent; no match returns `200` with `[]`                                                                                          | `BookServiceTest.findAllTreatsBlankFilterAsAbsent`, `BookControllerTest.emptyFiltersAreTreatedAsAbsent`, `filterBooksWithNoMatchReturnsEmptyArray`, and `BookApiFunctionalTest.filteringWithNoMatchReturnsEmptyArray`                                                                            | Pass   |
| Update changes author only; title in body is ignored; invalid or overlong author is rejected                                                           | `BookServiceTest.updateAuthorUpdatesExistingBook`, `updateAuthorThrowsInvalidWhenAuthorBlank`, `updateAuthorThrowsInvalidWhenAuthorTooLong`; controller `updateBookAuthorReturns200`, `updateBookIgnoresTitleInRequestBody`, and blank/oversized/missing-author tests                            | Pass   |
| Delete succeeds with `204`; missing ID returns `404` and `no book present`                                                                             | `BookServiceTest.deleteRemovesExistingBook`, `deleteThrowsNotFoundWhenIdMissing`, `BookControllerTest.deleteBookReturns204`, `deleteMissingBookReturns404`, and functional missing-delete check                                                                                                  | Pass   |
| Malformed JSON returns `400` with the approved message body                                                                                            | `BookControllerTest.createWithMalformedJsonReturns400`; actual `GlobalExceptionHandler` maps `HttpMessageNotReadableException`                                                                                                                                                                   | Pass   |
| Retain US-101 create/list API behavior and JSON responses                                                                                              | Controller `createBookReturns201` and `getAllBooksWithNoFilterReturnsEverything`; functional lifecycle performs create then list, and the browser functional case adds a book                                                                                                                    | Pass   |

### Current execution evidence

All commands ran on 2026-10-08 with Maven 3.9.9 and Java 21.0.9; the project
targets Java 17. Counts below are from these current executions, not historical
Step 5 evidence.

| Command                                   | Current result                                                             |
| ----------------------------------------- | -------------------------------------------------------------------------- |
| `mvn "-Dtest=BookServiceTest" test`       | PASS - 16 tests, 0 failures, 0 errors, 0 skipped                           |
| `mvn "-Dtest=BookControllerTest" test`    | PASS - 24 tests, 0 failures, 0 errors, 0 skipped                           |
| `mvn "-Dtest=BookApiFunctionalTest" test` | PASS - 7 TestNG tests, 0 failures, 0 errors, 0 skipped                     |
| `mvn test`                                | PASS - 40 JUnit tests plus 7 TestNG tests; 0 failures, 0 errors, 0 skipped |

The functional run used Chrome/ChromeDriver 154 and passed. Selenium logged
that it has no matching CDP implementation for Chrome 154; JVM dynamic-agent
and Spring `open-in-view` warnings were also non-failing. The current runs
verified US-101 create/list behavior; they did not repeat a process-restart
database persistence check.

### Document cross-check and LOW-1

The requirements, architecture, design decisions, plan, implementation report,
review, source, and tests were compared. Controller routes and service/repository
responsibilities match the architecture; the service validates before ID
lookup, and the approved title-ignore, blank-filter, error-body, and malformed
JSON decisions are implemented. Plan tasks and current test inventory match
the 2026-10-08 refresh. No requirements or behavior expansion was found.

LOW-1 is partly reconciled by dates and current evidence. Fresh runs reproduce
the October report's 16 service / 24 controller / 40 JUnit + 7 TestNG counts.
The worktree has one controller test beyond HEAD: the combined invalid-author,
missing-ID regression. Repository history also records earlier controller
edge-test changes and the 2026-10-01 formatting commit. The September report's
15 service / 20 controller / 35 JUnit + 7 TestNG figures are therefore treated
as historical execution counts, not current totals. Current source confirms
that the edge cases cited in the reports are present. However, the history does
not establish the exact first-add date for every service edge test or fully
substantiate which tests were added by the September run. LOW-1's precise
run-attribution wording remains a documentation follow-up; no claim about
unverified authorship or timing is made here. The prior restart-persistence
result in this file is historical and was not rerun on this date.

### Dependency safety

No standalone scanner executable was installed. For a no-dependency-change
advisory check, `mvn dependency:tree` generated the resolved project graph under
`target`, and the OSV.dev batch API was queried on 2026-10-08 for all 162
resolved Maven artifact/version pairs, including test scope. OSV returned 96
matches (95 distinct advisory IDs) across 22 artifact coordinates. Full OSV
records confirmed, among others:

- `org.apache.tomcat.embed:tomcat-embed-core:10.1.24` matches
  GHSA-23hv-mwm6-g8jf / CVE-2025-55668; the record lists 10.1.42 as fixed.
- `org.springframework:spring-context:6.1.8` matches
  GHSA-4gc7-5j7h-4qph / CVE-2024-38820; the record lists 6.1.14 as fixed.
- `com.fasterxml.jackson.core:jackson-databind:2.17.1` matches
  GHSA-3pjw-73gf-8qr5 / CVE-2026-59888; the record lists 2.18.8 as fixed.
- Test-scoped `io.github.bonigarcia:webdrivermanager:5.9.2` matches
  GHSA-pwm3-776c-8q7q / CVE-2025-4641; the record lists 6.1.0 as fixed.

This is a positive advisory finding, not a claim that every result is
exploitable in this application. Dependency safety is **FAIL** pending review
and remediation in the appropriate implementation step. No dependencies or
`pom.xml` were changed here. Advisory severity and application-specific
exploitability were not assessed in this verification.

### Final verdict

| Area              | Verdict  | Basis                                                                                                                                                                         |
| ----------------- | -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Code              | **PASS** | All US-102 matrix checks are covered by current tests; focused and full suites passed. No Step 5 feature rework was identified.                                               |
| Docs              | **FAIL** | Architecture and approved decisions match code, but LOW-1's precise historical test attribution cannot be fully verified; persistence was not rerun in the current execution. |
| Dependency safety | **FAIL** | OSV returned matching advisories for resolved production and test dependencies; remediation is outside this verification-only step.                                           |

Do not treat this report as approval to proceed to Step 8 until the documentation
follow-up and dependency findings have been reviewed. No implementation change
was made because Step 7 is verification-only.

## 2026-10-08 Rerun - Approved US-102

**Result: Code PASS; verification-document quality PASS with historical test
authorship dates unverified; dependency safety FAIL. Step 8 is not ready.**
This is a new execution record; the preceding dated sections are preserved as
historical reports. No production code, `pom.xml`, requirements, architecture,
design review, implementation plan, code review, or tests were modified during
this rerun. No coverage gap was found that warranted a test edit.

### Evidence attribution

- The 2026-09-22 Step 5 section in `docs/implementation.md` records that its
  run added update max-length, combined-filter, empty-filter, and omitted-field
  coverage. It records 15 service tests, 20 controller tests, and 35 JUnit
  plus 7 TestNG tests.
- The 2026-10-08 Step 5 reconciliation in `docs/impl-plan.md` and
  `docs/implementation.md` records that the service coverage was present when
  that refresh inspected the tree and that the combined invalid-author/missing-ID
  controller regression was the confirmed gap added in that run. Its recorded
  post-Step-5 totals are 16 service, 24 controller, and 40 JUnit plus 7 TestNG.
- This 2026-10-08 Step 7 rerun freshly executed those suites and reproduced
  16 service, 24 controller, 7 functional, and 40 JUnit plus 7 TestNG tests.
  These are current execution counts, not claims about when any individual
  test was first authored.

The dated artifacts describe successive runs and their reported test state;
they do not establish exact first-add dates for each test. No test authorship
date is inferred here. The earlier LOW-1 concern is resolved for execution
provenance by keeping those three records separate; exact test-origin dates
remain unverified and are not needed to substantiate this rerun's results.

### Verification matrix

| Requirement or approved decision                                                                                                       | Check                                                                                                                                                                                                                                                                                           | Result |
| -------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------ |
| Unfiltered listing, title-only and author-only partial case-insensitive filtering, combined AND filtering, and blank filters as absent | Service tests `findAllWithNoFiltersReturnsAllBooks`, `findAllWithTitleOnlyUsesTitleQuery`, `findAllWithAuthorOnlyUsesAuthorQuery`, `findAllWithBothFiltersUsesCombinedQuery`, and `findAllTreatsBlankFilterAsAbsent`; controller filter cases also ran                                          | PASS   |
| No match returns `200` and `[]`                                                                                                        | Controller `filterBooksWithNoMatchReturnsEmptyArray`; functional `filteringWithNoMatchReturnsEmptyArray`                                                                                                                                                                                        | PASS   |
| Create validation, duplicate detection, and malformed JSON preserve required status/message contracts                                  | Service validation/duplicate tests; controller missing, blank, oversized, duplicate, and `createWithMalformedJsonReturns400` cases; functional duplicate/invalid cases                                                                                                                          | PASS   |
| PUT changes author only, ignores title, rejects invalid author, and preserves approved validation-before-lookup precedence             | Controller `updateBookIgnoresTitleInRequestBody`, `updateNonexistentBookWithBlankAuthorReturns400`, and `updateMissingBookReturns404`; service update validation cases                                                                                                                          | PASS   |
| DELETE returns `204`; a missing ID returns `404` with the required message                                                             | Controller `deleteBookReturns204`, `deleteMissingBookReturns404`; functional missing-delete case                                                                                                                                                                                                | PASS   |
| Existing US-101 create/list behavior and end-to-end US-102 workflow                                                                    | Functional `fullLifecycle_createFilterUpdateDelete`; controller create/list cases; complete functional suite                                                                                                                                                                                    | PASS   |
| File-backed H2 record survives an application process restart                                                                          | Packaged app ran twice on port 18083 using `target/verify-bookshelf-rerun`; first process created UUID `cd07e115-e442-431d-a96d-85ccb7e74a07`; second process returned the same UUID, title, and author. Deleted the verification record and confirmed a subsequent `GET /books` returned `[]`. | PASS   |

### Current execution evidence

All Maven executions used Java 21.0.9; `pom.xml` targets Java 17. The
functional suite ran against Chrome/ChromeDriver 154. Selenium logged that no
matching CDP implementation was available, and Maven tests emitted dynamic
JVM-agent and Spring `open-in-view` warnings; the warnings were non-failing.

| Command                                   | Result                                                                                                                                  |
| ----------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| `mvn "-Dtest=BookServiceTest" test`       | PASS - 16 tests, 0 failures, 0 errors, 0 skipped                                                                                        |
| `mvn "-Dtest=BookControllerTest" test`    | PASS - 24 tests, 0 failures, 0 errors, 0 skipped; includes invalid-author/missing-ID `400` and valid-author/missing-ID `404` assertions |
| `mvn "-Dtest=BookApiFunctionalTest" test` | PASS - 7 TestNG tests, 0 failures, 0 errors, 0 skipped                                                                                  |
| `mvn test`                                | PASS - 40 JUnit tests plus 7 TestNG tests, 0 failures, 0 errors, 0 skipped                                                              |
| `mvn -DskipTests package`                 | PASS - packaged `target/bookshelf-api-0.1.0.jar` for the process-restart check                                                          |

The functional test suite was re-run by `mvn test` as well as by its focused
command; its full-suite TestNG execution also passed all 7 tests.

### Document and implementation cross-check

| Check                                          | Result                                                                                                                                                                                                                                      |
| ---------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Requirements against source and tests          | PASS - current controller/service behavior and the executed coverage match the approved US-102 statuses, messages, filtering, update, and delete criteria.                                                                                  |
| Architecture routes, components, and data flow | PASS - GET/POST `/books` and PUT/DELETE `/books/{id}` are present; controller delegates to service, repository queries match filter/duplicate behavior, and exception mappings match documented contracts.                                  |
| Approved design-review decisions               | PASS - ignored PUT title, blank-filter behavior, `message` error shape, unreadable-body handling, and invalid-author precedence are implemented and covered. Accepted entity/API coupling and non-atomic duplicate check remain documented. |
| Plan and implementation reports                | PASS for current status and dated execution counts - the September and October records are separated above; exact test-origin dates are not claimed.                                                                                        |
| Existing code-review output                    | PASS - current review reports no blocking code findings; this rerun independently executes the requested gates.                                                                                                                             |

The production-source diff present in the worktree was inspected and consists
of formatting/import normalization; no production behavior change was found.
This is an observation about the current diff, not a claim that production
files were untouched by earlier workflow steps.

### Dependency advisory check

For this rerun, a fresh resolved graph was generated with
`mvn org.apache.maven.plugins:maven-dependency-plugin:3.8.1:tree "-DoutputType=json" "-DoutputFile=target/dependency-tree-rerun.json"`
and all 162 unique dependency coordinates were queried using OSV.dev's batch
API on 2026-10-08. The graph included active compile/runtime and test-scoped
direct and transitive project dependencies: 64 production-scope coordinates
and 98 test-scope coordinates. OSV returned 96 matching advisory records
(95 distinct advisory IDs) across 22 coordinates: 13 production-scope and 9
test-scope coordinates were affected.

Representative exact-version matches confirmed against OSV records:

- `org.apache.tomcat.embed:tomcat-embed-core:10.1.24` -
  `GHSA-23hv-mwm6-g8jf` / `CVE-2025-55668` (production scope).
- `org.springframework:spring-context:6.1.8` -
  `GHSA-4gc7-5j7h-4qph` / `CVE-2024-38820` (production scope).
- `com.fasterxml.jackson.core:jackson-databind:2.17.1` -
  `GHSA-3pjw-73gf-8qr5` / `CVE-2026-59888` (production scope).
- `io.github.bonigarcia:webdrivermanager:5.9.2` -
  `GHSA-pwm3-776c-8q7q` / `CVE-2025-4641` (test scope).

**Dependency safety: FAIL pending human review and remediation.** An OSV
version/advisory match does not by itself establish reachability or
application-specific exploitability. This check reflects the currently
resolved default Maven graph and OSV's online data at query time; it does not
scan Maven build plugins, alternate profiles/platform graphs, or prove that
every advisory is exploitable. No dependency or POM change was made.

### Final verdict

| Area              | Verdict  | Basis                                                                                                                                                                                                           |
| ----------------- | -------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Code              | **PASS** | Service, controller, functional, full Maven, and process-restart checks passed; the combined PUT `400` and missing-ID `404` assertions passed. No coverage gap or production behavior defect was identified.    |
| Docs              | **PASS** | Current requirements/design/plan/source/test behavior is consistent. This report now distinguishes September Step 5, October Step 5, and this fresh rerun without asserting unverifiable test authorship dates. |
| Dependency safety | **FAIL** | OSV returned advisories for resolved production and test dependencies; review/remediation is outside this Step 7 scope.                                                                                         |

**Step 8 gate: BLOCKED.** The workflow permits Step 8 only after verification
passes; the dependency advisory failure must be reviewed and addressed before
proceeding. Step 8 was not invoked.

## 2026-10-08 — Step 7: Approved Spring Boot 4.0.8 remediation

**Result: Code PASS; documents PASS; dependency safety PASS for the audited
resolved project graph.** Historical verification sections above are retained
as records of their original results. This section records fresh execution
after the approved Boot 4.0.8 remediation. Step 8 was not performed.

### Verification matrix

| Requirement or approved decision                                                                              | Current verification                                                                                                                                                    | Result |
| ------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------ |
| Filter behavior: unfiltered, title-only, author-only, combined AND, blank filters, and no matches as `200 []` | Focused service and controller suites; full functional suite. The executed suites cover the corresponding service/controller filter tests and functional no-match case. | PASS   |
| Create validation and duplicate contracts, including malformed JSON and exact error messages                  | Focused service/controller suites and functional suite; controller tests cover missing, blank, oversized, duplicate, and malformed payloads.                            | PASS   |
| PUT changes author only, ignores title, and rejects invalid author with the required `400` message            | Focused service/controller suites; `updateBookIgnoresTitleInRequestBody` and author validation cases.                                                                   | PASS   |
| Combined PUT precedence: invalid author plus missing ID returns `400` with `please check the request payload` | `updateNonexistentBookWithBlankAuthorReturns400` ran in the controller suite; its assertion is present in the fresh full-suite XML report.                              | PASS   |
| Valid author plus missing ID returns `404` with `no book present`                                             | `updateMissingBookReturns404` ran in the controller suite; its assertion is present in the fresh full-suite XML report.                                                 | PASS   |
| DELETE returns `204`; missing ID returns `404` with the required message                                      | Focused service/controller suites and functional suite.                                                                                                                 | PASS   |
| Existing US-101 create/list behavior remains intact                                                           | Functional suite and packaged-app GET/POST smoke.                                                                                                                       | PASS   |
| Java 17 compiler target and packaged application startup                                                      | Effective `maven.compiler.release` is `17`; packaged jar started on JDK 21.0.9 with isolated in-memory H2.                                                              | PASS   |

### Fresh execution evidence

All Maven commands ran on Windows 11 with Maven 3.9.9 and Java 21.0.9. The
project's compiler release evaluated to `17`; the packaged app log identified
Java 21.0.9. The focused functional run used Chrome/ChromeDriver 154. Non-
failing warnings included Mockito/Byte Buddy dynamic-agent loading, Spring's
`open-in-view` default in test contexts, and Selenium's missing matching CDP
implementation for Chrome 154.

| Command                                                                    | Observed result                                                                                                                                                                                                                              |
| -------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `mvn "-Dtest=BookServiceTest" test`                                        | PASS — 16 JUnit tests, 0 failures, 0 errors, 0 skipped.                                                                                                                                                                                      |
| `mvn "-Dtest=BookControllerTest" test`                                     | PASS — console shows 24 JUnit tests, 0 failures, 0 errors, 0 skipped. This isolated run then reports 0 tests from the configured TestNG provider; that provider overwrites the same-named focused Surefire report with its zero-test result. |
| `mvn "-Dtest=BookApiFunctionalTest" test`                                  | PASS — 7 TestNG tests, 0 failures, 0 errors, 0 skipped.                                                                                                                                                                                      |
| `mvn test`                                                                 | PASS — 40 JUnit tests and 7 TestNG tests, 0 failures, 0 errors, 0 skipped.                                                                                                                                                                   |
| `mvn -DskipTests package`                                                  | PASS — executable `target/bookshelf-api-0.1.0.jar` built.                                                                                                                                                                                    |
| `mvn help:evaluate "-Dexpression=maven.compiler.release" -q -DforceStdout` | PASS — `17`.                                                                                                                                                                                                                                 |

The full-suite run resolves review finding LOW-2: its final
`target/surefire-reports/com.example.bookshelf.controller.BookControllerTest.txt`
reports 24 tests, 0 failures/errors/skips, and
`target/surefire-reports/TEST-com.example.bookshelf.controller.BookControllerTest.xml`
has `tests="24"`, `failures="0"`, `errors="0"`, and `skipped="0"`. The XML
contains both `updateNonexistentBookWithBlankAuthorReturns400` and
`updateMissingBookReturns404`. Thus the fresh full-run reports substantiate the
24-test controller result; the zero-test artifact was specific to the focused
multi-provider invocation, not a missing controller execution.

The packaged application started on port 18084 with
`jdbc:h2:mem:verify-step7;DB_CLOSE_DELAY=-1`. Smoke results: initial GET
`200 []`; POST `201` with a returned ID; follow-up GET `200` contained that
record; DELETE cleanup returned `204`; final GET returned `200 []`. The app
was stopped after the check. This was an isolated in-memory smoke, not a
process-restart persistence test.

### Dependency audit

Regenerated the resolved graph with Maven Dependency Plugin 3.8.1 at
`target/dependency-tree-step7.json`, then queried every unique Maven
coordinate/version through OSV.dev's `https://api.osv.dev/v1/querybatch`. The
fresh full response is `target/osv-step7-fresh.json`. It contains 176 unique
results for 176 graph coordinates: 75 production-scope and 101 test-scope
coordinates, with no missing, extra, or duplicate query entries. The response
has 0 matching coordinates, 0 advisory records, and 0 unique advisory IDs.
An independent reconciliation of the previously saved post-remediation graph
and response also found 176/176 accounted for and zero vulnerability results.

**Dependency safety: PASS** for the regenerated default resolved project graph
and its compile/runtime/test scopes at query time. This does not audit Maven
build-plugin dependencies, alternate profiles, or platform-specific graphs,
and does not establish future OSV status.

### Document and implementation cross-check

Requirements, architecture, design-review decisions, implementation plan,
implementation evidence, Step 6 review, source, and test assertions were
checked against each other. Routes remain GET/POST `/books` and PUT/DELETE
`/books/{id}`; controller delegation, service validation-before-ID-lookup,
error messages, filtering, and approved update/delete behavior agree with the
current source and executed tests. The plan and implementation report identify
Boot 4.0.8, Java 17 target, and the remediation gates. Prior histories remain
untouched; current results and the LOW-2 report behavior are distinguished
from historical counts and earlier blocked candidates. No missing acceptance
coverage or document-quality inconsistency was found.

| Area              | Verdict  | Basis                                                                                                                                                                   |
| ----------------- | -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Code              | **PASS** | All focused suites and the full suite passed; the controller's final full-run report confirms 24 tests and the two required PUT outcomes. Package and API smoke passed. |
| Documents         | **PASS** | Current source, tests, requirements, architecture, plan, implementation report, and review agree; LOW-2 is resolved by fresh full-run report evidence.                  |
| Dependency safety | **PASS** | Fresh 176-coordinate OSV batch query had zero matches; every graph coordinate is accounted for.                                                                         |

**Step 7 result: PASS. Step 8 was not invoked.** No POM, production source,
test source, or other documentation was changed in this verification run.
