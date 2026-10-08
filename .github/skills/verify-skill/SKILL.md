---
name: verify-skill
description: "Use for Step 7 verification in the Bookshelf API SDLC workflow after code review and before PR creation. Generates and runs comprehensive unit, integration, functional, and document quality checks, then reports pass/fail evidence."
---

# Verify Skill

## Role
You generate and run a comprehensive verification suite for the completed change. Verify both the code and the final output documents. You may add or update tests and verification artifacts, but you do not implement feature fixes unless the human explicitly asks.

## Procedure
1. Read the requirements, architecture, design review, implementation plan, code review findings, and changed files.
2. Build a verification matrix that maps each acceptance criterion and documented edge case to at least one check.
3. Generate or update tests when coverage is missing.
4. Run unit tests for service and validation logic.
5. Run integration tests for API routes, status codes, response bodies, and error handling.
6. Run functional or UI checks when the change affects end-to-end behavior.
7. Verify final output documents for content quality, including completeness, consistency with implemented behavior, clear decisions, real test evidence, and no fabricated claims.
8. Cross-check `docs/architecture.md` route and component descriptions against the actual code.
9. Treat missing tests, failing tests, undocumented behavior changes, and document-quality gaps as failed verification items.
10. Report failures for the human or implementation step to address, then re-verify once fixed.

## Code Verification Coverage
- Unit tests for business logic, validation, and edge cases.
- Integration tests for controller/API behavior.
- Functional or UI tests for user-visible flows when applicable.
- Error-path checks for invalid input, missing fields, not-found cases, duplicate/conflicting state, and empty states.

## Document Quality Coverage
- Requirements match implemented behavior.
- Architecture matches actual routes, models, components, and data flow.
- Design-review decisions are reflected or tracked.
- Implementation plan status is consistent with completed work.
- Final output documents include real evidence instead of fabricated or paraphrased test results.
