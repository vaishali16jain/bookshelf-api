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

| Review area | Verdict | Evidence |
|---|---|---|
| Correctness | **Pass** | The implementation matches US-102: filtering is partial and case-insensitive, combined filters use AND, no matches return `[]`, PUT changes only author, DELETE returns 204, and create validation and duplicate handling use the required statuses and messages. No production behavior changed during rework. |
| Security | **Pass** | No secrets are exposed. Input is validated before writes, and Spring Data derived queries parameterize filter and duplicate values. Entity-as-request-model exposure remains the explicitly accepted design-review item #2. |
| Error Handling | **Pass** | Domain exceptions map to the required 400/404 responses, unreadable JSON maps to the approved 400 body, empty repositories return `[]`, and missing update/delete IDs are handled. The new tests confirm omitted fields reach the same approved invalid-payload response. |
| Test Coverage | **Pass** | Happy paths, all filter combinations, empty/no-match behavior, duplicate creation, blank/oversized/malformed payloads, not-found paths, ignored PUT title, and all three required omitted-field cases are covered. Focused review execution passed 20 controller tests with 0 failures, errors, or skips. Step 5 also records a full-suite pass of 35 JUnit plus 7 TestNG tests. |
| Code Clarity | **Pass** | Production methods remain small, linear, and named by behavior. The new test names clearly state each missing-field contract; their inconsistent indentation is non-blocking. |
| DRY Principle | **Pass** | Required/max-length validation remains centralized in `BookService.validateField`; the rework introduces no duplicated production logic. |
| Dependency Safety | **Pass** | The rework introduces no dependency changes. No specific vulnerable dependency was established by this review; automated vulnerability scanning and a supported Spring Boot patch upgrade remain prudent non-blocking maintenance. |

### Step 7 Input

Step 6 is complete and may proceed to Step 7 after the workflow's normal human
approval. Verification should retain the three omitted-field controller cases
and confirm the reported full-suite result. The accepted DTO-coupling and
single-user duplicate-check limitations remain out of scope as documented in
`docs/design-review.md`.

### Outcome

The prior finding is closed, every checklist area passes, and no blocking
finding remains. This review did not perform Step 7 or modify production code.