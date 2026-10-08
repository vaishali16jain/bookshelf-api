# Code Review

## 2026-09-22 - SCRUM-2 / US-102 Re-review

Status: **Complete - ready for Step 7**

Reviewed the approved Step 5 rework against `docs/requirements.md`, the
architecture, design review, implementation plan, implementation report, and
the current working-tree diff. The rework changes tests and documentation only;
no production code or dependencies were changed.

### Findings

No blocking findings remain.

The prior medium-severity test-coverage finding is **resolved**.
`BookControllerTest` now exercises syntactically valid POST JSON missing
`title`, POST JSON missing `author`, and PUT JSON missing `author`. Each test
asserts `400 Bad Request` and the exact
`"please check the request payload"` message, so the omitted-field HTTP
contract is directly protected rather than inferred from blank-field tests.

One non-blocking clarity note remains: the three added test methods use deeper
indentation than neighboring methods. This does not affect behavior or make the
review fail, but normalizing the formatting in a future cleanup would improve
consistency.

### Checklist

| Review area       | Verdict  | Evidence                                                                                                                                                                                                                                                                                                                                                                         |
| ----------------- | -------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Correctness       | **Pass** | The implementation matches US-102: filtering is partial and case-insensitive, combined filters use AND, no matches return `[]`, PUT changes only author, DELETE returns 204, and create validation and duplicate handling use the required statuses and messages. No production behavior changed during rework.                                                                  |
| Security          | **Pass** | No secrets are exposed. Input is validated before writes, and Spring Data derived queries parameterize filter and duplicate values. Entity-as-request-model exposure remains the explicitly accepted design-review item #2.                                                                                                                                                      |
| Error Handling    | **Pass** | Domain exceptions map to the required 400/404 responses, unreadable JSON maps to the approved 400 body, empty repositories return `[]`, and missing update/delete IDs are handled. The new tests confirm omitted fields reach the same approved invalid-payload response.                                                                                                        |
| Test Coverage     | **Pass** | Happy paths, all filter combinations, empty/no-match behavior, duplicate creation, blank/oversized/malformed payloads, not-found paths, ignored PUT title, and all three required omitted-field cases are covered. Focused review execution passed 20 controller tests with 0 failures, errors, or skips. Step 5 also records a full-suite pass of 35 JUnit plus 7 TestNG tests. |
| Code Clarity      | **Pass** | Production methods remain small, linear, and named by behavior. The new test names clearly state each missing-field contract; their inconsistent indentation is non-blocking.                                                                                                                                                                                                    |
| DRY Principle     | **Pass** | Required/max-length validation remains centralized in `BookService.validateField`; the rework introduces no duplicated production logic.                                                                                                                                                                                                                                         |
| Dependency Safety | **Pass** | The rework introduces no dependency changes. No specific vulnerable dependency was established by this review; automated vulnerability scanning and a supported Spring Boot patch upgrade remain prudent non-blocking maintenance.                                                                                                                                               |

### Step 7 Input

Step 6 is complete and may proceed to Step 7 after the workflow's normal human
approval. Verification should retain the three omitted-field controller cases
and confirm the reported full-suite result. The accepted DTO-coupling and
single-user duplicate-check limitations remain out of scope as documented in
`docs/design-review.md`.

### Outcome

The prior finding is closed, every checklist area passes, and no blocking
finding remains. This review did not perform Step 7 or modify production code.

## 2026-10-08 - SCRUM-2 / US-102 plan refresh

### Finding

**LOW-1 - Run attribution in implementation evidence is ambiguous.** The
2026-09-22 section of `docs/implementation.md` says "this run" added update
max-length, combined AND, empty-filter, and omitted-field coverage. The
2026-10-08 `docs/impl-plan.md` says those service tests already existed and
only the combined invalid-author/nonexistent-ID controller test was new in
that refresh. These can describe separate runs, but the wording is unclear.
Counts also differ by date: earlier 15 service / 20 controller, 35 JUnit + 7
TestNG; 2026-10-08 16 service / 24 controller, 40 JUnit + 7 TestNG. The
working diff reformats production Java files; "No production code was changed"
should say "No production behavior was changed."

**Risk/recommendation:** Verification may misattribute coverage or reuse
historical totals. Label claims by date and describe Java edits as
formatting-only. No behavioral gap was established.

### Evidence

Reviewed requirements, architecture, design review, plan, implementation
report, actual diff, relevant production/tests, and POM. `pom.xml` is unchanged.
The regression clears storage, sends blank author to
`PUT /books/missing-id`, and asserts 400 plus the required message; valid
author/missing-ID expects 404. `updateAuthor` validates author before lookup,
so the test truly covers approved precedence. I ran it: 1 passed, 0
failures/errors/skips. Broad 2026-10-08 totals are report evidence, not rerun
here; the focused run replaced the controller Surefire report with one-test
results.

### Checklist

| Area              | Verdict                      | Evidence                                                                                                               |
| ----------------- | ---------------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| Correctness       | **PASS**                     | US-102 service rules and response contracts match; precedence test passed.                                             |
| Security          | **PASS**                     | Inputs validated; no secrets exposed. Entity/API coupling and check-then-save duplicate detection are accepted risks.  |
| Error handling    | **PASS**                     | Required 400/404 messages, unreadable-body, empty-result, and missing-ID behavior are handled.                         |
| Test coverage     | **PASS**                     | Create, filter, update/delete, malformed input, and combined PUT cases exist; only the precedence test was rerun here. |
| Clarity           | **PASS**                     | Business rules are in service; production diff is formatting/import-only with no behavior change found.                |
| DRY               | **PASS**                     | Create/update share `validateField`.                                                                                   |
| Dependency safety | **NEEDS HUMAN CONFIRMATION** | No dependency changed; no vulnerability/advisory scan ran.                                                             |

### Step 7 inputs

- Run the planned service, controller, functional, and full Maven suites; save
  output to confirm reported totals.
- Retain combined PUT 400/message and valid-author/missing-ID 404 assertions.
- Scan resolved Maven dependencies and report advisories; clarify LOW-1 claims.
  Step 7 was not invoked.

### Outcome

No production correctness/security defect found. LOW-1 remains; dependency
status needs confirmation. All checklist areas have explicit verdicts.

## 2026-10-08 - Approved dependency remediation re-review

### Finding

**LOW-2 - The retained controller Surefire report does not substantiate the
reported 24-test pass.** The current
`target/surefire-reports/com.example.bookshelf.controller.BookControllerTest.txt`
and matching XML report both record 0 tests, while the approved remediation
notes and user-provided post-hook result report 24 passing tests. The source
contains 24 controller test methods (23 at HEAD plus the combined
invalid-author/missing-ID regression), and the expected US-102 assertions are
present, but the durable execution artifact conflicts with the stated run.

**Classification/risk:** LOW; verification evidence only. This does not
identify a code or assertion defect. Reconcile the report with the actual
post-hook command output, and ensure a focused controller run records all 24
tests in the later verification evidence. This review did not rerun tests or
invoke Step 7.

### Review scope and evidence

Reviewed the approved US-102 requirements, architecture, design review,
implementation plan and notes, current POM diff, full current test sources and
diff, generated resolved dependency graph, effective dependency versions,
and complete saved OSV batch response. No requirements, routes, models,
production sources, or US-102 behavior changed by the remediation; dependency
versions and test compatibility changed as described below.

The POM moves the parent from Spring Boot 3.3.0 to 4.0.8 and retains Java 17.
It imports Jackson 3.1.7 and Jackson 2.21.7 BOMs, adds Boot 4's
`spring-boot-starter-webmvc-test` and the BOM-managed JUnit Platform launcher,
and aligns both explicit Surefire providers with the parent plugin version
3.5.6. The resolved graph confirms Jackson 3 core/databind 3.1.7, Jackson 2
core/databind 2.21.7 (annotations 2.21), and JUnit Platform components and
launcher 6.0.3. Surefire and TestNG discovery are reported passing in the
implementation evidence; no provider/JUnit version mismatch was found.

RestAssured changed from 5.5.0 to 6.0.0 alongside its resolved Groovy 5.0.8
runtime. This is test-scoped and addresses the documented Groovy 5 request-path
failure; the implementation records the functional and full suites passing.
The test-source compatibility edits switch the controller test to Jackson 3's
`tools.jackson.databind.ObjectMapper` and Boot 4's MVC test annotation. The
service suite remains 16 methods and the functional suite 7; the controller
suite adds only the approved precedence regression, for 24 methods total.
Current test sources still assert US-102's required validation/error
contracts, partial and combined filters, empty results, author-only update,
ignored PUT title, missing-ID behavior, delete status, and malformed JSON.
No removed acceptance assertion was found.

### OSV audit verification

Independently parsed `target/osv-after-remediation-fresh.json` and reconciled
its result coordinates against every unique node in
`target/dependency-tree-after-remediation.json`. The graph has 176 unique
coordinates (75 production and 101 test); the OSV response has 176 unique
entries, with zero graph coordinates missing, zero extra entries, zero
non-empty vulnerability results, and embedded totals of 0 matching
coordinates / 0 advisory records / 0 unique advisory IDs. The artifact names
OSV.dev `querybatch` as its source and records query time
`2026-10-08T06:23:25.2304040Z`. Therefore dependency safety passes for this
resolved project graph and audited scopes. Maven build-plugin dependencies,
alternate profiles, and platform-specific graphs are outside that artifact's
stated scope.

### Checklist

| Area              | Verdict                | Evidence                                                                                                                                                                                                                                                                                                 |
| ----------------- | ---------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Correctness       | **PASS**               | Boot 4 changes preserve Java 17 and approved routes/contracts; current service, controller, and functional sources retain US-102 behavior and assertions. No production behavior change was reported or found.                                                                                           |
| Security          | **PASS**               | No secrets or authentication behavior were introduced; validation remains covered and no security/API behavior regression was identified. Authentication remains intentionally out of scope for the approved single-user prototype.                                                                      |
| Error handling    | **PASS**               | Required 400/404/204 statuses and messages, malformed JSON handling, empty results, and invalid-author-before-ID precedence remain asserted in current tests.                                                                                                                                            |
| Test coverage     | **NEEDS CONFIRMATION** | All 16 service, 24 controller, and 7 functional test methods/contracts are present, and implementation notes report 40 JUnit + 7 TestNG passing. However, the retained latest controller Surefire `.txt` and XML both report 0 tests; reconcile execution evidence before relying on the 24-test result. |
| Code clarity      | **PASS**               | Compatibility imports match Boot 4/Jackson 3 APIs. The broad Java diff is formatter/import churn plus one focused controller regression; no behavior-changing test edit was identified.                                                                                                                  |
| DRY principle     | **PASS**               | No production logic was duplicated or changed in this dependency-only remediation.                                                                                                                                                                                                                       |
| Dependency safety | **PASS**               | The complete saved post-remediation OSV response independently reconciles to all 176 unique resolved project coordinates and has zero matches. This verdict is limited to the graph/scopes audited.                                                                                                      |

### Outcome

No production correctness, API, security, or dependency finding blocks the
remediation. Dependency safety is independently verified as **PASS**. The
review remains **NEEDS CONFIRMATION** only for the contradictory retained
controller execution report; Step 7 was not invoked.
