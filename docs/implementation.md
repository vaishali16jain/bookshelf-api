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

| Command | Result |
|---|---|
| `mvn test "-Dtest=BookServiceTest"` | PASS — 15 tests, 0 failures, 0 errors, 0 skipped |
| `mvn test "-Dtest=BookControllerTest"` | PASS — 20 tests, 0 failures, 0 errors, 0 skipped |
| `mvn test "-Dtest=BookApiFunctionalTest"` | PASS — 7 tests, 0 failures, 0 errors, 0 skipped |
| `mvn test` | PASS — 35 JUnit tests and 7 TestNG tests, 0 failures, 0 errors, 0 skipped |

The test run emitted non-failing JVM dynamic-agent and Selenium CDP-version
warnings. They do not affect the tested US-102 behavior and are not Step 5
blockers.

### Remaining blockers

None. Separate API DTOs and atomic duplicate enforcement remain intentionally
out of scope per the approved design review.