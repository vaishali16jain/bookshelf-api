# Implementation

## 2026-09-22 — SCRUM-2 / US-102

Status: **Complete**

### Approved scope delivered

- Moved `BookRepository` to its own component and added the four approved
  case-insensitive duplicate/filter queries (plan tasks #1-#2).
- Added the three domain exceptions, `ErrorResponse`, and centralized HTTP
  exception handling, including malformed JSON (tasks #3-#5).
- Added `BookService` validation and create, filter, author-update, and delete
  behavior (tasks #6-#10). Blank filters are treated as absent, and the shared
  field validation enforces required values and the 255-character maximum.
- Updated `BookController` to delegate to the service and expose the approved
  GET, POST, PUT, and DELETE behavior (tasks #11-#14). PUT reads only the
  incoming author, so title remains unchanged.
- Added service, controller integration, and black-box functional coverage for
  the approved success and failure paths (tasks #15-#16).

### Step 6 review rework

Human-approved review rework for task #16 added focused MockMvc coverage for
POST payloads missing `title`, POST payloads missing `author`, and PUT payloads
missing `author`. Each test asserts `400 Bad Request` and the exact
`"please check the request payload"` message. All three tests passed against
the existing implementation, so no production code was changed.

The production implementation for tasks #1-#14 and most planned tests was
already present when this Step 5 run began. This run inspected that work,
added explicit coverage for update max-length validation, combined AND
filtering, empty filters, and omitted required fields, and completed the
required documentation.

### Alignment with approved decisions

- Uses the existing Spring Boot, Spring Data JPA, and file-based H2 design.
- Keeps validation and persistence decisions in `BookService`.
- Keeps the existing `Book` request/response model, as accepted in design
  review item #2.
- Keeps the single-user check-then-save duplicate strategy, as accepted in
  design review item #3.
- Returns errors using the approved single-field `{"message":"..."}` shape.
- Adds no endpoints, models, persistence technologies, or dependencies beyond
  the approved architecture.

### Validation evidence

| Command                                   | Result                                                                    |
| ----------------------------------------- | ------------------------------------------------------------------------- |
| `mvn test "-Dtest=BookServiceTest"`       | PASS — 15 tests, 0 failures, 0 errors, 0 skipped                          |
| `mvn test "-Dtest=BookControllerTest"`    | PASS — 20 tests, 0 failures, 0 errors, 0 skipped                          |
| `mvn test "-Dtest=BookApiFunctionalTest"` | PASS — 7 tests, 0 failures, 0 errors, 0 skipped                           |
| `mvn test`                                | PASS — 35 JUnit tests and 7 TestNG tests, 0 failures, 0 errors, 0 skipped |

The test run emitted non-failing JVM dynamic-agent and Selenium CDP-version
warnings. They do not affect the tested US-102 behavior and are not Step 5
blockers.

### Remaining blockers

None. Separate API DTOs and atomic duplicate enforcement remain intentionally
out of scope per the approved design review.

## 2026-10-08 — Approved US-102 plan refresh

Status: **Complete**

### Plan reconciliation

The approved refresh was checked against the current repository before editing.
Tasks #1-#5 were already satisfied: `BookRepository` is in its own file with
the approved derived queries; exception and error-response handling preserves
the required statuses/messages and malformed-body mapping; service validation,
filtering, duplicate, update, and delete behavior match the approved design;
and the controller delegates the existing GET, POST, PUT, and DELETE routes.
In particular, `updateAuthor` validates the author before looking up the ID.

Task #6 was already covered by `BookServiceTest`, including shared required
and maximum-length validation, duplicate detection, filter combinations and
blank filters, and update/delete success and not-found cases. The only
confirmed gap was task #7: no controller regression combined an invalid
author with a nonexistent ID. Added that focused MockMvc case; it asserts
`400 Bad Request` and `"please check the request payload"`. No production
changes were needed. Task #8's existing end-to-end functional coverage passed,
including the retained US-101 create/list path. Requirements were not changed.

### Validation evidence

| Command                                                                               | Result                                                                    |
| ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- |
| `mvn "-Dtest=BookControllerTest#updateNonexistentBookWithBlankAuthorReturns400" test` | PASS — 1 test, 0 failures, 0 errors, 0 skipped                            |
| `mvn "-Dtest=BookServiceTest" test`                                                   | PASS — 16 tests, 0 failures, 0 errors, 0 skipped                          |
| `mvn "-Dtest=BookControllerTest" test`                                                | PASS — 24 tests, 0 failures, 0 errors, 0 skipped                          |
| `mvn "-Dtest=BookApiFunctionalTest" test`                                             | PASS — 7 tests, 0 failures, 0 errors, 0 skipped                           |
| `mvn test`                                                                            | PASS — 40 JUnit tests and 7 TestNG tests, 0 failures, 0 errors, 0 skipped |

The Selenium functional and full-suite runs reported that Selenium has no CDP
implementation matching Chrome 154; the tests still passed. Non-failing JVM
dynamic-agent warnings were also emitted.

## 2026-10-08 — Dependency remediation continuation

Status: **Blocked — the test and application gates do not all pass**

### Approved dependency change

Inspection of the effective POM found Maven Surefire 3.5.6, but the POM
overrode both `surefire-junit-platform` and `surefire-testng` to 3.2.5. The
project graph resolved Jupiter and JUnit Platform engine 6.0.3. Updated both
providers to 3.5.6 and declared `junit-platform-launcher` in test scope without
a version, allowing the Spring Boot 4.0.8 BOM to select 6.0.3. This preserves
both providers and changes no production code or unrelated dependencies.

The provider version alignment alone still failed with
`OutputDirectoryCreator not available`; adding the BOM-managed launcher
resolved that focused discovery failure. Surefire loaded both configured
providers, and the focused service suite passed.

### Fresh OSV comparison

On 2026-10-08, queried OSV.dev's `https://api.osv.dev/v1/querybatch` for every
unique resolved Maven coordinate/version in the original pre-remediation
graph and the final graph regenerated after the runner fix. The baseline is
`target/dependency-tree-before-remediation.json` (Spring Boot 3.3.0); the final
graph is `target/dependency-tree-after-runner-fix.json` (Spring Boot 4.0.8,
including the managed JUnit Platform launcher). Full OSV responses are in
`target/osv-before-remediation-fresh.json` and
`target/osv-after-runner-fix-fresh.json`.

| Graph              | Resolved coordinates | Production scope | Test scope |                                                             OSV matches |
| ------------------ | -------------------: | ---------------: | ---------: | ----------------------------------------------------------------------: |
| Before remediation |                  162 |               64 |         98 | 96 records, 95 unique IDs across 22 coordinates (13 production, 9 test) |
| After runner fix   |                  176 |               75 |        101 |                                          0 records across 0 coordinates |

The fresh after graph has no OSV matches. The query included all resolved
project dependency scopes; Maven build-plugin dependencies were not part of
the project dependency graph audit.

### Validation evidence

All Maven checks used Maven 3.9.9 with Java 21.0.9; the compiler reported
`--release 17`. The packaged app smoke test also used JDK 21.0.9.

| Command                                                                        | Result                                                                                                                      |
| ------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------- |
| `mvn "-Dtest=BookServiceTest" test`                                            | PASS — 16 tests, 0 failures, 0 errors, 0 skipped                                                                            |
| `mvn "-Dtest=BookControllerTest" test`                                         | FAIL — 24 errors; the existing tests require a Jackson 2 `ObjectMapper` bean, absent from the Boot 4 Jackson 3 test context |
| `mvn "-Dtest=BookApiFunctionalTest" test`                                      | FAIL — 7 tests, 4 failures; failures are `NullPointerException`s in the existing RestAssured request path                   |
| `mvn test`                                                                     | FAIL — 40 JUnit tests (16 pass, 24 errors) and 7 TestNG tests (4 failures)                                                  |
| `mvn -DskipTests package`                                                      | PASS — executable jar created                                                                                               |
| Packaged app startup and `GET /books` on port 18081 with isolated in-memory H2 | PASS — HTTP 200 and a JSON array; repository file-backed H2 data was not used or changed                                    |

The controller and functional failures are outside the requested test-runner
alignment. No production, test-source, or unrelated dependency changes were
made to address them. Step 5 remains blocked on these gates despite the clean
OSV comparison, focused service pass, successful package, and smoke check.

## 2026-10-08 — Spring Boot 3.5.16 compatibility candidate

Status: **Blocked — the focused controller build cannot compile the existing test source**

### Dependency-only candidate

Changed the parent to Spring Boot 3.5.16, restored
`spring-boot-starter-test`, removed the Jackson 3/Jackson 2 BOM overrides and
the Boot 4-era `junit-platform-launcher` declaration, and aligned both
explicit Surefire providers through the parent property
`maven-surefire-plugin.version` (effective value: 3.5.6). No application or
test source was changed. Spring Boot 3.5.16 resolves Jackson 2.21.4, and the
compiler confirmed `--release 17`.

### Focused validation

Environment: Maven 3.9.9, Java 21.0.9 on Windows 11; compiler target Java 17.

| Command                                                                           | Result                                                                                                                                                                                      |
| --------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `mvn "-Dtest=BookControllerTest" test`                                            | FAIL before test execution — test compilation cannot resolve `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` (package does not exist; annotation symbol missing). |
| `mvn help:evaluate "-Dexpression=maven-surefire-plugin.version" -q -DforceStdout` | PASS — 3.5.6.                                                                                                                                                                               |

The existing test imports a Boot 4 package that is absent from the Boot 3.5
test starter. Resolving this incompatibility would require changing test source
or reintroducing a Boot 4 test module into the Boot 3 graph; neither is within
the approved constraints. The focused test did not execute. Service,
functional, full-suite, package, smoke, and candidate dependency-audit checks
were not run. The previous Boot 4.0.8 OSV result does not establish the
Boot 3.5.16 graph's status. Step 5 remains blocked; Steps 6-8 were not run.

### Compatibility and validation continuation

Status: **Blocked — the required application gates pass, but the complete
Boot 3.5.16 graph still has OSV matches**

Applied only the approved test-source compatibility change: replaced the
Boot 4 `AutoConfigureMockMvc` import with Boot 3.5's
`org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc`.
No test behavior, production code, or POM dependency was changed in this
continuation.

Environment: Maven 3.9.9 ran on Java 21.0.9 (Windows 11); Maven compiler
output confirmed `--release 17`, and `mvn help:evaluate
"-Dexpression=maven.compiler.release" -q -DforceStdout` returned `17`.

| Command/check                                      | Result                                                                                                                                        |
| -------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| `mvn "-Dtest=BookControllerTest" test`             | PASS — 24 tests, 0 failures, 0 errors, 0 skipped                                                                                              |
| `mvn "-Dtest=BookServiceTest" test`                | PASS — 16 tests, 0 failures, 0 errors, 0 skipped                                                                                              |
| `mvn "-Dtest=BookApiFunctionalTest" test`          | PASS — 7 tests, 0 failures, 0 errors, 0 skipped                                                                                               |
| `mvn test`                                         | PASS — 40 JUnit tests and 7 TestNG tests, 0 failures, 0 errors, 0 skipped                                                                     |
| `mvn -DskipTests package`                          | PASS — executable jar created                                                                                                                 |
| Packaged API smoke on port 18082 with in-memory H2 | PASS — `GET /books` returned 200; `POST /books` returned 201; subsequent GET returned the created book. No file-backed project data was used. |

The test runs emitted non-failing Mockito/ByteBuddy dynamic-agent warnings.
The functional and full-suite runs also warned that Selenium has no CDP
implementation matching Chrome 154; all tests still passed.

### Fresh Boot 3.5.16 OSV audit

Queried OSV.dev `https://api.osv.dev/v1/querybatch` on 2026-10-08 at
`06:07:04Z` for every unique resolved coordinate/version in the regenerated
`target/dependency-tree-after-remediation.json`. All 158 queries returned a
result: 63 coordinates were compile/runtime scope and 95 were test scope.
The full response is retained in `target/osv-after-remediation-fresh.json`.

| Graph                                                                | Resolved coordinates | Production scope | Test scope | Advisory records / IDs | Affected coordinates |
| -------------------------------------------------------------------- | -------------------: | ---------------: | ---------: | ---------------------: | -------------------: |
| Previous baseline (`target/dependency-tree-before-remediation.json`) |                  162 |               64 |         98 |                96 / 95 |                   22 |
| Boot 3.5.16 candidate                                                |                  158 |               63 |         95 |                15 / 15 |                    5 |

Remaining candidate matches:

| Coordinate and scope                                           | Advisory IDs                                                                                                                                                                                                  |
| -------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `org.springframework:spring-webmvc:6.2.19` (compile)           | `GHSA-j9f9-w8pj-32f8`, `GHSA-pc63-qcmh-9cmg`                                                                                                                                                                  |
| `com.fasterxml.jackson.core:jackson-core:2.21.4` (compile)     | `GHSA-7hhh-6rmp-j9qf`, `GHSA-p6pp-m3f8-5c89`                                                                                                                                                                  |
| `com.fasterxml.jackson.core:jackson-databind:2.21.4` (compile) | `GHSA-5gvw-p9qm-jgwh`, `GHSA-5jmj-h7xm-6q6v`, `GHSA-cxp5-3px4-pw24`, `GHSA-gx83-3vf8-gh7j`, `GHSA-mhm7-754m-9p8w`, `GHSA-q4xh-88c3-wmh7`, `GHSA-vvgp-rfg2-7rr6`, `GHSA-wjgm-6hv5-3cvf`, `GHSA-wv8q-qhhj-9h54` |
| `org.apache.logging.log4j:log4j-api:2.24.3` (compile)          | `GHSA-qv9r-c865-cp47`                                                                                                                                                                                         |
| `org.apache.commons:commons-lang3:3.17.0` (test)               | `GHSA-j288-q9x7-2f5v`                                                                                                                                                                                         |

The query is complete, but its result is not clean: dependency safety does not
pass for this candidate. No further dependency changes were made in this
continuation. Step 5 remains blocked on the 15 current OSV matches. The
implementation and plan evidence were updated; `docs/verify.md` was not
edited, and Steps 6-8 were not performed.

## 2026-10-08 — Spring Boot 4.0.8 remediation and test compatibility

Status: **Step 5 complete; zero OSV matches**

### Dependency and test compatibility changes

Restored the previously audited Spring Boot 4.0.8 parent and MVC test setup.
Kept Java 17, Tomcat 11.0.26, Selenium 4.31.0, and WebDriverManager 6.1.0;
aligned Jackson 3 and Jackson 2 BOMs to 3.1.7 and 2.21.7, respectively. The
Boot-managed JUnit Platform launcher resolves to 6.0.3. Both explicit
Surefire providers resolve to 3.5.6. Updated test-scoped RestAssured from
5.5.0 to 6.0.0 after the Boot 4 graph selected Groovy 5.0.8 and RestAssured
5.5.0 failed within its Groovy HTTP request path before reaching the API.

Two test-only source compatibility edits were made in
`BookControllerTest`: use Jackson 3's `tools.jackson.databind.ObjectMapper`
and Boot 4's `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`.
No assertions or acceptance behavior changed. `BookServiceTest` and
`BookApiFunctionalTest` sources were unchanged. No production source or
behavior changed.

### Validation evidence

Environment: Maven 3.9.9 on Windows 11 with Java 21.0.9; compilation targets
Java 17 (`maven.compiler.release=17`). Effective Surefire version: 3.5.6.

| Command/check                                | Result                                                                                                                            |
| -------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| `mvn "-Dtest=BookControllerTest" test`       | PASS — 24 tests, 0 failures, 0 errors, 0 skipped                                                                                  |
| `mvn "-Dtest=BookServiceTest" test`          | PASS — 16 tests, 0 failures, 0 errors, 0 skipped                                                                                  |
| `mvn "-Dtest=BookApiFunctionalTest" test`    | PASS — 7 tests, 0 failures, 0 errors, 0 skipped                                                                                   |
| `mvn test`                                   | PASS — 40 JUnit tests and 7 TestNG tests, 0 failures, 0 errors, 0 skipped                                                         |
| `mvn -DskipTests package`                    | PASS — executable jar created                                                                                                     |
| Packaged API smoke, port 18083, in-memory H2 | PASS — initial GET returned 0; POST returned 201; follow-up GET returned the created book. File-backed project data was not used. |

The shell's bare `java` resolved to Java 8 and could not start the Boot 4 jar;
the smoke test was rerun successfully using the JDK 21 executable explicitly.
Tests emitted non-failing Mockito/ByteBuddy dynamic-agent warnings. Selenium
reported no CDP implementation matching Chrome 154; functional assertions
passed.

### Fresh OSV audit

Regenerated `target/dependency-tree-after-remediation.json` with Maven
Dependency Plugin 3.8.1 and queried every unique resolved Maven coordinate
and version, including compile/runtime and test scopes, through OSV.dev's
`https://api.osv.dev/v1/querybatch`. The complete response is in
`target/osv-after-remediation-fresh.json`, queried at
`2026-10-08T06:23:25.2304040Z`.

| Resolved coordinates | Production scope | Test scope | Matching coordinates | Advisory records / unique IDs |
| -------------------: | ---------------: | ---------: | -------------------: | ----------------------------: |
|                  176 |               75 |        101 |                    0 |                         0 / 0 |

The full dependency graph is clean under this query. `docs/verify.md` was not
modified. Step 6 review and Steps 7-8 remain sequenced separately and were
not performed here.
