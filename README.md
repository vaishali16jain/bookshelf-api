# Bookshelf API — Agentic SDLC Capstone

A minimal Java/Spring Boot prototype (US-101: add + view books) set up as
the starting point for the capstone's 8-step Agentic SDLC exercise.

Books are stored in a local file-based H2 database under `data/` and remain
available after the application restarts.

Open `http://localhost:8080/` in a browser to use the bookshelf UI. It lists
saved books and provides a form for calling the `POST /books` API.

## Run it

```bash
mvn spring-boot:run
```

Then, in another terminal:
```bash
curl -X POST localhost:8080/books -H "Content-Type: application/json" \
  -d '{"title":"Dune","author":"Frank Herbert"}'

curl localhost:8080/books
```

## Run the tests

```bash
# All tests (unit + integration + functional/UI)
mvn test

# Integration tests only (BookControllerTest, MockMvc)
mvn test -Dtest=BookControllerTest

# Functional/automation tests only (BookApiFunctionalTest, REST Assured + Selenium, TestNG)
mvn test -Dtest=BookApiFunctionalTest
```

## What's already here
- `src/` — the prototype implementation of US-101 (intentionally minimal,
  with a known validation gap left in on purpose — see `docs/design-review.md`)
- `docs/requirements.md` — US-101 written up as your baseline story
- `docs/architecture.md`, `design-review.md`, `impl-plan.md` — stubs, each
  with the exact Copilot Chat prompt to run to fill them in
- `.github/copilot-instructions.md` — rules applied to every Copilot session
- `.github/agents/` — six role-specific instruction files, one per SDLC
  step: requirements, architect, reviewer (design + code review), planner,
  QA, PR

## Next steps
1. Commit this as-is: `git add -A && git commit -m "Prototype: US-101 baseline"`
2. Bring in a real requirement change (e.g. "add a read/unread status and
   let users filter by it") and run it through all 8 steps using the
   agents in `.github/agents/`, in order:
   requirements → architecture → design review → planning → implementation
   → code review → verify → PR.
3. Each agent file tells you what prompt to give it — start with:
   ```
   #file:.github/agents/requirements-agent.md
   #file:docs/requirements.md
   ```
