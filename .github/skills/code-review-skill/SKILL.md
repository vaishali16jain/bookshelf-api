---
name: code-review-skill
description: "Use for Step 6 structured code review in the Bookshelf API SDLC workflow after implementation and before PR creation. Acts as a peer reviewer and evaluates correctness, security, error handling, test coverage, clarity, DRY, and dependency safety."
---

# Code Review Skill

## Role
You perform a structured code review of the completed implementation before PR creation. You act as a peer reviewer, not as the implementer.

## Procedure
1. Read the requirements and the implementation diff before making findings.
2. Use GitHub Copilot Chat or CLI context to inspect the changed files and nearby code.
3. Evaluate every checklist area below.
4. Lead with findings, ordered by severity.
5. For each issue, include the affected file or area, the risk, and the recommended fix.
6. If an area passes, state the pass explicitly.
7. Do not silently approve. Every checklist area must receive a pass, fail, or needs-human-confirmation note.
8. Do not modify code in this step unless the human explicitly asks for the review fixes to be implemented.

## Code Review Checklist

| Review Area | Review Question |
|---|---|
| Correctness | Does each component behave as specified in `docs/requirements.md`? |
| Security | Are secrets excluded from output? Is user input validated? |
| Error Handling | Are all API failures, missing files, and empty repos handled gracefully? |
| Test Coverage | Do tests cover the happy path and the not-found or missing-field edge cases? |
| Code Clarity | Are function names self-explanatory? Is logic easy to follow without comments? |
| DRY Principle | Is there duplicated logic that can be refactored into a shared function? |
| Dependency Safety | Are any known-vulnerable package versions introduced or left unaddressed? |
