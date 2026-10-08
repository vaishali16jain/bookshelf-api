# Implementation Plan

## 2026-10-08 Boot 4 Remediation Completion

This status supersedes the later-dated Boot 3.5.16 candidate blocker below.
Restored Spring Boot 4.0.8 and its MVC test starter with Java 17, Jackson BOMs
3.1.7/2.21.7, Tomcat 11.0.26, Selenium 4.31.0, and WebDriverManager 6.1.0.
JUnit Platform launcher resolves to 6.0.3; both Surefire providers resolve to
3.5.6. RestAssured was updated from 5.5.0 to 6.0.0 to work with the resolved
Groovy 5.0.8 runtime.

Test-only edits in `BookControllerTest`: changed the mapper import to
`tools.jackson.databind.ObjectMapper` and restored Boot 4's
`org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`.
No assertions, functional/service tests, production source, or behavior changed.

| Gate                                             | Result                                                                                                                            |
| ------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------- |
| Controller / service / functional suites         | PASS — 24 / 16 / 7 tests; no failures, errors, or skips                                                                           |
| Full `mvn test`                                  | PASS — 40 JUnit + 7 TestNG tests; no failures, errors, or skips                                                                   |
| `mvn -DskipTests package` and packaged API smoke | PASS — executable jar; isolated in-memory H2 GET 0, POST 201, follow-up GET shows created record                                  |
| Java target / runtime                            | PASS — compiler release 17; Maven 3.9.9 and app on Java 21.0.9; Surefire 3.5.6                                                    |
| Fresh OSV audit                                  | PASS — 176/176 resolved coordinates queried (75 production, 101 test); 0 matches / 0 advisories at `2026-10-08T06:23:25.2304040Z` |

Graph and audit evidence: `target/dependency-tree-after-remediation.json` and
`target/osv-after-remediation-fresh.json`. `docs/verify.md` was not modified;
Step 6 and Steps 7-8 remain sequenced separately.

## US-102 tasks — Filter, update, and delete books

Source: `docs/architecture.md` § US-102, `docs/design-review.md` (2026-09-22, human-approved).

| #   | Task                                                                                                                                                                                                                                                                                                            | Depends on                  | Blocked?                                             |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------- | ---------------------------------------------------- |
| 1   | Move `BookRepository` out of `BookController.java` into its own file (`repository/BookRepository.java`), no behavior change.                                                                                                                                                                                    | [no deps]                   | No                                                   |
| 2   | Add derived query methods to `BookRepository`: `existsByTitleIgnoreCaseAndAuthorIgnoreCase`, `findByTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase`, `findByTitleContainingIgnoreCase`, `findByAuthorContainingIgnoreCase`.                                                                             | #1                          | Yes — needs the repository in its own file first     |
| 3   | Create exception classes `BookNotFoundException`, `InvalidBookException`, `DuplicateBookException` (unchecked, each carrying the exact required message text).                                                                                                                                                  | [no deps]                   | No                                                   |
| 4   | Create `ErrorResponse` record (single `message` field) for the error JSON body.                                                                                                                                                                                                                                 | [no deps]                   | No                                                   |
| 5   | Create `GlobalExceptionHandler` (`@RestControllerAdvice`) mapping each exception from #3 to its HTTP status and mapping `HttpMessageNotReadableException` to `400`, using `ErrorResponse` (#4) as the body.                                                                                                     | #3, #4                      | Yes — needs exceptions and `ErrorResponse` to exist  |
| 6   | Create `BookService` with a private `validateField(value)` shared helper (required + max 255 chars).                                                                                                                                                                                                            | #3                          | Yes — throws `InvalidBookException` from #3          |
| 7   | Implement `BookService.create(title, author)`: call `validateField` (#6), check duplicate via `existsByTitleIgnoreCaseAndAuthorIgnoreCase` (#2), throw `DuplicateBookException` (#3) or save.                                                                                                                   | #2, #3, #6                  | Yes                                                  |
| 8   | Implement `BookService.findAll(titleFilter, authorFilter)`: treat blank/absent params as "no filter", pick the matching repository query (#2).                                                                                                                                                                  | #2                          | Yes                                                  |
| 9   | Implement `BookService.updateAuthor(id, author)`: call `validateField` (#6), load by id or throw `BookNotFoundException` (#3), update, save.                                                                                                                                                                    | #3, #6                      | Yes                                                  |
| 10  | Implement `BookService.delete(id)`: load by id or throw `BookNotFoundException` (#3), delete.                                                                                                                                                                                                                   | #3                          | Yes                                                  |
| 11  | Update `BookController`: delegate `POST /books` to `BookService.create` (#7); remove direct `BookRepository` calls.                                                                                                                                                                                             | #7                          | Yes                                                  |
| 12  | Update `BookController`: add optional `title`/`author` query params to `GET /books`, delegate to `BookService.findAll` (#8).                                                                                                                                                                                    | #8                          | Yes                                                  |
| 13  | Add `PUT /books/{id}` to `BookController`, reading only `author` from the body (ignore any `title`), delegate to `BookService.updateAuthor` (#9).                                                                                                                                                               | #9                          | Yes                                                  |
| 14  | Add `DELETE /books/{id}` to `BookController` returning `204`, delegate to `BookService.delete` (#10).                                                                                                                                                                                                           | #10                         | Yes                                                  |
| 15  | Unit tests for `BookService`: shared required/max-length validation on create and update, case-insensitive duplicate detection, not-found update/delete, and filter combinations (none/title/author/both/blank).                                                                                                | #6, #7, #8, #9, #10         | Yes — exercises all service behavior                 |
| 16  | Integration tests for `BookController` + `GlobalExceptionHandler`: existing create/list behavior; filter/update/delete happy paths and statuses; PUT title ignored; empty filters treated as absent; duplicate, invalid, not-found, and malformed-JSON failures with exact status and `{"message":"..."}` body. | #5, #11, #12, #13, #14, #15 | Yes — exercises the completed service and HTTP stack |

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

### Validation sequence

1. Run focused `BookServiceTest` tests after #15; all create, filter, update, delete, validation, duplicate, and not-found cases must pass.
2. Run focused `BookControllerTest` tests after #16; verify exact HTTP statuses and error response bodies, including malformed JSON and ignored PUT titles.
3. Run `BookApiFunctionalTest` to confirm the user-visible create/list/filter/update/delete workflow and preserve US-101 behavior.
4. Run the full Maven test suite as the final Step 5 completion check.

### Planning blockers and approval gate

## US-102 tasks — Plan refresh (2026-10-08)

Scope: approved US-102 only, based on `docs/architecture.md` and the
2026-10-08 decisions in `docs/design-review.md`. This refresh preserves the
2026-09-22 plan and completion history above. Step 5 must inspect the current
implementation and tests before changing anything; no current implementation
or test coverage is assumed here. Keep any changes within US-102.

| #   | Task                                                                                                                                                                                                                                                                                                                                                                                             | Depends on     | Blocked?                                                                |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------------- | ----------------------------------------------------------------------- |
| 1   | Inspect the current US-102 repository, service, exception handling, controller, and related tests against the approved architecture and acceptance criteria. Record which behaviors and tests already exist and which are missing, including whether `updateAuthor` validates author before ID lookup and whether a controller test asserts the combined invalid-author/nonexistent-ID response. | [no deps]      | No                                                                      |
| 2   | Update `BookRepository` only where the inspection (#1) finds gaps: keep it in its own repository file and provide the derived queries needed for case-insensitive duplicate checks and partial filtering.                                                                                                                                                                                        | #1             | Yes — inspection must identify any repository gaps first                |
| 3   | Update US-102 exception and response handling only where gaps exist: preserve exact required messages/statuses and the `{"message":"..."}` response contract, including malformed/unreadable JSON mapping.                                                                                                                                                                                       | #1             | Yes — inspection must identify any exception-handling gaps first        |
| 4   | Update `BookService` only where gaps exist: required/nonblank and 255-character validation, shared validation for create/update, case-insensitive duplicate detection, blank-filter handling and AND filtering, not-found handling, and author validation before ID lookup for `updateAuthor`.                                                                                                   | #2, #3         | Yes — required repository operations and exceptions must be available   |
| 5   | Update `BookController` only where gaps exist: retain create/list behavior, delegate filtering and business rules to the service, and implement the approved update/delete HTTP contracts, including ignoring PUT `title`.                                                                                                                                                                       | #3, #4         | Yes — error handling and service behavior must be available             |
| 6   | Inspect and update `BookServiceTest` for any coverage gaps in US-102 create validation/duplicates, filter combinations and blank filters, update validation/author-only mutation/not-found behavior, and delete success/not-found behavior.                                                                                                                                                      | #2, #3, #4, #5 | Yes — production behavior must be reconciled before tests are finalized |
| 7   | Inspect and update `BookControllerTest` for any coverage gaps in US-102 statuses and response bodies, including a focused regression test that sends an invalid author to `PUT /books/{id}` with a nonexistent ID and asserts `400 Bad Request` plus `"please check the request payload"`.                                                                                                       | #3, #5, #6     | Yes — controller behavior and service tests must be reconciled first    |
| 8   | Run/update `BookApiFunctionalTest` as needed to verify the US-102 end-to-end filter/update/delete flows and ensure existing US-101 create/list behavior remains intact.                                                                                                                                                                                                                          | #6, #7         | Yes — service and controller tests must pass first                      |

### Dependency order and blocked work

1. Inspect the current state (#1); this is the only unblocked task.
2. Reconcile repository and exception-handling gaps (#2 and #3); these can be
   handled independently after inspection.
3. Reconcile service behavior (#4), then controller wiring and HTTP behavior
   (#5).
4. Tests are last: service coverage (#6), controller coverage including the
   approved combined PUT error case (#7), then functional verification (#8).

Tasks #2-#8 are blocked until their listed dependencies are complete. These
are sequencing dependencies, not unresolved design questions. Do not duplicate
already-correct implementation or tests; mark satisfied tasks as verified
during Step 5 and make changes only for confirmed gaps.

### Validation gates

1. After task #6, run `mvn -Dtest=BookServiceTest test`; all applicable US-102
   service cases must pass.
2. After task #7, run `mvn -Dtest=BookControllerTest test`; verify the combined
   PUT case returns status 400 and the exact invalid-payload message, along
   with the other applicable controller contracts.
3. After task #8, run `mvn -Dtest=BookApiFunctionalTest test`; verify US-102

### Planning status and approval gate

No unresolved design blocker remains within approved US-102. The combined PUT
precedence is fixed by the approved design decision: author validation comes
before ID lookup, so the combined failure returns `400 Bad Request` with
`"please check the request payload"`. This plan does not add a requirement or
expand scope.

**Approval gate (satisfied 2026-10-08):** The user approved this exact

### 2026-10-08 Step 5 execution status

The user approved this exact plan refresh for Step 5 execution. The current
implementation and tests were inspected before edits; existing satisfied work
was retained.

| Task | Status   | Reconciliation                                                                                                                                                |
| ---- | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| #1   | Complete | Inspected current repository, service, exception handling, controller, and tests.                                                                             |
| #2   | Verified | Repository location and derived query methods already satisfy the task; no change needed.                                                                     |
| #3   | Verified | Exception statuses/messages, `message` response, and unreadable-body mapping already satisfy the task; no change needed.                                      |
| #4   | Verified | Service validation, duplicate detection, blank/combined filters, not-found handling, and validation-before-lookup already satisfy the task; no change needed. |
| #5   | Verified | Controller routes and service delegation already satisfy the task; no change needed.                                                                          |
| #6   | Verified | Existing service tests cover the planned behavior; no change needed.                                                                                          |
| #7   | Complete | Added controller regression for blank author plus nonexistent ID; asserts 400 and the exact invalid-payload message.                                          |
| #8   | Complete | Existing functional suite passed, including US-102 flows and retained US-101 behavior.                                                                        |

Step 5 is complete. See `docs/implementation.md` for the focused changes and
validation evidence. Requirements and production behavior were unchanged.

## US-102 tasks — Dependency advisory remediation (2026-10-08)

Source: the 2026-10-08 Step 7 rerun in `docs/verify.md`; user-approved Step 5
rework scope. This dated addendum preserves all prior plan and execution
history. Scope is dependency safety only: do not change features, routes,
models, persistence behavior, or tests except as required to keep existing
tests compatible with confirmed dependency updates. Do not add dependencies,
suppress advisories, or modify scanner/audit results. No additional human
approval gate is required for this approved rework.

The verification report records 96 OSV advisory matches across 22 Maven
coordinates (13 production-scope and 9 test-scope), but gives exact details for
only four representative coordinates. Step 5 must regenerate the current graph
and obtain the complete advisory and fixed-version guidance before editing;
the representative findings are not a substitute for the full triage.

| #   | Task                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      | Depends on | Blocked?                                                                   |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------- | -------------------------------------------------------------------------- |
| 1   | Capture the untouched dependency baseline: record the active Maven profiles/scopes and Java/Maven versions; regenerate a JSON dependency tree using `mvn org.apache.maven.plugins:maven-dependency-plugin:3.8.1:tree "-DoutputType=json" "-DoutputFile=target/dependency-tree-before-remediation.json"`; enumerate every resolved group/artifact/version coordinate, including compile/runtime and test scopes.                                                                                                                                                           | [no deps]  | No                                                                         |
| 2   | Query OSV.dev for every unique resolved Maven coordinate/version in the regenerated graph. Reconcile the result against the reported 22 affected coordinates; record every affected coordinate, scope, advisory ID, matched version/range, and exact fixed-version guidance (or explicitly record when OSV supplies none). Confirm the four representative findings against the refreshed records rather than assuming the old graph is unchanged.                                                                                                                        | #1         | Yes — exact findings and guidance must be current before choosing upgrades |
| 3   | Select the smallest supported Spring Boot parent/BOM release that supports Java 17 and coherently brings managed production dependencies to versions outside every applicable affected range. Check its managed versions against all production findings from #2; prefer the BOM over individual production transitive pins. If no supported Java 17 Boot release resolves the findings, or the only compatible resolution requires work outside dependency updates, stop before editing and report the exact conflict.                                                   | #2         | Yes — full production triage is required                                   |
| 4   | Update the Spring Boot parent version in `pom.xml` to the release selected in #3, retaining Java 17 and existing dependencies/features. Add a production dependency override only if the selected supported BOM leaves a confirmed affected production coordinate unresolved and OSV's fixed guidance supports the override; document why it is necessary.                                                                                                                                                                                                                | #3         | Yes — blocked until a supported, coherent BOM target is established        |
| 5   | Update a directly declared test dependency only when #2 confirms that its resolved version is affected, or #3/#4 makes it incompatible. Use the exact fixed-version guidance when available, confirm compatibility with the existing JUnit/TestNG/Selenium/Rest Assured test setup, and do not add or replace dependencies. Leave unaffected direct test dependencies unchanged.                                                                                                                                                                                          | #2, #4     | Yes — dependency evidence and production baseline must be settled first    |
| 6   | Reconcile the post-edit dependency graph and audit evidence: regenerate JSON to `target/dependency-tree-after-remediation.json`, query every resolved coordinate/version in all audited scopes against OSV, and compare before/after coordinate and finding inventories. Record graph counts, affected-coordinate/advisory counts, exact remaining matches (if any), and query date/source.                                                                                                                                                                               | #4, #5     | Yes — all dependency edits must be complete                                |
| 7   | Validate the application and existing tests without changing feature behavior: run the focused service, controller, and functional suites, then `mvn test`, `mvn -DskipTests package`, and a packaged-application startup plus existing-API smoke check. Confirm the Maven compiler still targets Java 17; record the actual runtime used. Repair only dependency-caused compatibility failures within the approved dependency-only scope.                                                                                                                                | #4, #5     | Yes — final dependency set must resolve before validation                  |
| 8   | Record implementation evidence in `docs/implementation.md` and append the dated Step 7 rerun to `docs/verify.md`: include exact dependency changes and reasons, before/after OSV graph methodology/counts, all remaining matches or zero-match result, Java/Maven environment, commands and actual test/package/startup results, and any warnings or limitations. Set dependency safety to PASS only when the full post-change audit has no OSV matches in the audited resolved graph and all required app/test checks pass; otherwise retain FAIL and state the blocker. | #6, #7     | Yes — both audit and application validation evidence are required          |

### Dependency order and blockers

1. Establish and fully triage the fresh baseline (#1-#2).
2. Select and apply the Java 17-compatible supported Spring Boot BOM (#3-#4).
3. Make evidence-backed direct test dependency updates, if needed (#5).
4. Regenerate and audit the complete graph (#6), then run application/test
   gates (#7). These checks may be executed in parallel after all dependency
   edits are complete, but both must pass before documentation is finalized.
5. Record evidence and final dependency-safety verdict (#8).

Tasks #2-#8 are blocked by their listed dependencies. The absence of all 22
exact coordinates and their fixed-version guidance from the current report is
an explicit pre-edit blocker, not permission to infer fixes from the four
examples. If OSV supplies no fixed release for any currently affected
coordinate, if a clean supported Java 17 Spring Boot baseline cannot be
selected, or if resolution requires source/feature changes, stop and report
the unresolved findings and compatibility constraint without expanding scope.

### Required audit and validation gates

1. The pre-edit OSV inventory must cover every unique resolved Maven
   coordinate/version in the regenerated graph, including production and test
   scopes. Preserve the complete 22-coordinate reconciliation and exact
   advisory/fixed-version mapping as Step 5 evidence.
2. The post-edit OSV audit must use the regenerated graph, not declared POM
   versions alone, and query all resolved coordinates in the same scopes and
   manner as the baseline. PASS requires zero matching advisories for the
   resulting graph. Any match, omitted coordinate, failed query, or
   unreconciled baseline finding blocks Step 8; do not suppress or waive it.
3. Run `mvn "-Dtest=BookServiceTest" test`,
   `mvn "-Dtest=BookControllerTest" test`,
   `mvn "-Dtest=BookApiFunctionalTest" test`, `mvn test`, and
   `mvn -DskipTests package`. All existing tests must pass, and the packaged
   app must start and pass a basic existing API smoke check. Record observed
   counts/results; do not claim historical test counts as current evidence.
4. Confirm the project still compiles for Java 17 and record the JDK used to
   run Maven/application checks. Dependency-induced failures may be resolved
   only by in-scope dependency updates; a required source or feature change is
   a stop-and-report condition.
5. Step 8 may proceed only after the complete dependency audit has zero
   matches, application/test/package checks pass, and the dated implementation
   and verification evidence is recorded. This is the already-approved
   rework's completion criterion, not a new human approval gate.

### Planning status

The remediation scope is approved for Step 5. No exact upgrade versions are
preselected because the verification report omits 18 affected coordinates and
does not provide complete fixed-version guidance. Step 5 begins with task #1;
do not edit dependencies until task #2 is complete. Any blocker requiring
scope beyond dependency updates must be reported without proceeding to Step 8.

### 2026-10-08 Step 5 dependency-remediation continuation

| Task | Status   | Evidence                                                                                                                                                                                                                      |
| ---- | -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| #1   | Complete | Baseline graph has 162 coordinates (64 production, 98 test). Maven 3.9.9, Java 21.0.9, external `default` profile; POM compiler targets Java 17.                                                                              |
| #2   | Complete | Fresh OSV batch query of the baseline found 96 matches (95 unique IDs) across 22 coordinates: 13 production and 9 test. Full records are in `target/osv-before-remediation-fresh.json`.                                       |
| #3   | Verified | The selected Java 17-compatible Spring Boot 4.0.8 graph has zero fresh OSV matches after remediation.                                                                                                                         |
| #4   | Verified | Parent is already Spring Boot 4.0.8; no parent or production dependency change was made in this continuation.                                                                                                                 |
| #5   | Complete | Aligned both Surefire providers from 3.2.5 to effective plugin version 3.5.6 and added the test-scope JUnit Platform launcher, BOM-managed at 6.0.3. JUnit and TestNG providers both load.                                    |
| #6   | Complete | Final graph has 176 coordinates (75 production, 101 test); fresh OSV query returned zero matches. Graph and full response are in `target/dependency-tree-after-runner-fix.json` and `target/osv-after-runner-fix-fresh.json`. |
| #7   | Blocked  | Service suite, package, and isolated packaged-app smoke check pass. Controller tests have 24 Jackson 2 `ObjectMapper` context errors; functional tests have 4 RestAssured request-path failures; full suite therefore fails.  |
| #8   | Blocked  | Implementation evidence is updated, but this task cannot be marked complete until task #7's existing test-stack failures are resolved and all required gates pass.                                                            |

**Step 5 status: BLOCKED.** The runner discovery mismatch is fixed and the
fresh OSV comparison is clean, but the controller, functional, and full-suite
gates remain failing. No production or test-source edits were made; resolving
the Jackson 2/Jackson 3 test-context mismatch and RestAssured failures would
expand beyond the approved runner-only POM adjustment. Step 6, Step 7, and
Step 8 were not started.

### 2026-10-08 Spring Boot 3.5.16 candidate follow-up

The approved narrower Java 17 candidate was tested without application or
test-source changes. The parent is now 3.5.16, the Boot 3
`spring-boot-starter-test` is restored, Jackson BOM overrides and the
Boot-4-era launcher declaration are removed, and explicit Surefire providers
use `${maven-surefire-plugin.version}` (effective value 3.5.6).

| Check                                  | Result                                                                                                                                                                                             |
| -------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `mvn "-Dtest=BookControllerTest" test` | Blocked at test compilation: the existing source imports Boot 4's `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`, unavailable in the Boot 3.5 test graph. No tests ran. |
| Boot 3.5.16 effective Surefire version | 3.5.6; provider property resolves.                                                                                                                                                                 |
| Candidate graph and OSV audit          | Not run because the focused compatibility gate failed. The earlier clean Boot 4.0.8 audit does not apply to this candidate.                                                                        |
| Remaining Step 5 gates                 | Not run.                                                                                                                                                                                           |

**Step 5 remains BLOCKED.** Preserving the current test sources with this
supported Boot 3 candidate would require a Boot 4 test module or source edits;
neither is an acceptable dependency-only resolution. Stop here without Steps
6-8. Maven 3.9.9 ran on Java 21.0.9, and compilation targeted Java 17.

### 2026-10-08 compatibility and validation continuation

The approved test-source compatibility edit changed only the
`AutoConfigureMockMvc` import to Boot 3.5's package. No test behavior,
production code, or POM dependency changed. The earlier candidate-follow-up
section remains the historical record of the initial compile blocker.

| Gate                       | Result                                                                                                                                |
| -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| Focused controller         | PASS — 24 tests, 0 failures/errors/skips                                                                                              |
| Service                    | PASS — 16 tests, 0 failures/errors/skips                                                                                              |
| Functional                 | PASS — 7 tests, 0 failures/errors/skips                                                                                               |
| Full Maven suite           | PASS — 40 JUnit and 7 TestNG tests, 0 failures/errors/skips                                                                           |
| Package                    | PASS — `mvn -DskipTests package` created the executable jar                                                                           |
| Packaged API smoke         | PASS — isolated in-memory H2; GET 200, POST 201, and the created book appeared in GET                                                 |
| Java target/runtime        | PASS — compiler `--release 17`; Maven 3.9.9 and packaged application ran on Java 21.0.9                                               |
| Fresh full-graph OSV query | COMPLETE, NOT CLEAN — 158/158 coordinates queried; 63 production-scope and 95 test-scope coordinates; 15 records across 5 coordinates |

Passing tests emitted non-failing Mockito/ByteBuddy dynamic-agent warnings;
Selenium also reported no CDP implementation for Chrome 154.

The remaining OSV matches are `org.springframework:spring-webmvc:6.2.19`
(2 IDs), `com.fasterxml.jackson.core:jackson-core:2.21.4` (2),
`com.fasterxml.jackson.core:jackson-databind:2.21.4` (9),
`org.apache.logging.log4j:log4j-api:2.24.3` (1), and
`org.apache.commons:commons-lang3:3.17.0` (1). All IDs and query results are
recorded in `docs/implementation.md` and
`target/osv-after-remediation-fresh.json`. The earlier baseline had 162
coordinates and 96 advisory records across 22 coordinates; the candidate's
fresh scan does not establish a clean dependency graph.

**Step 5 remains BLOCKED** because dependency safety requires zero OSV matches.
The implementation and plan evidence were updated; `docs/verify.md` was not
edited as directed. No Step 6 review or Steps 7-8 workflow work was performed.
