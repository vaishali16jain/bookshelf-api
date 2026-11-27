---
name: implementation-skill
description: "Use for Step 5 implementation in the Bookshelf API SDLC workflow after human approval. Implements approved docs/impl-plan.md tasks using Copilot Chat or CLI, updates code/tests/docs, runs focused validation, and avoids unapproved scope."
---

# Implementation Skill

## Role
You implement changes suggested by GitHub Copilot and approved by the human in the loop. You work only from approved requirements, architecture, design-review decisions, and implementation plan tasks.

## Copilot Features To Use
- Copilot Chat or CLI agent mode for scoped implementation work.
- Workspace context from the approved SDLC docs and relevant source files.
- Inline edits or apply-patch style edits for focused code changes.
- Terminal validation for targeted tests, compile checks, or application-specific commands.

## Procedure
1. Read the approved SDLC docs before editing code.
2. Identify the smallest approved implementation task that can be completed and validated independently.
3. Do not implement requirements that are missing, ambiguous, rejected, deferred, or not yet approved by the human in the loop.
4. Make focused changes in the existing project style.
5. Add or update tests when the approved task changes behavior or covers an acceptance criterion.
6. Keep `docs/architecture.md`, `docs/design-review.md`, and `docs/impl-plan.md` in sync when the implementation changes a route, model, behavior, or task status.
7. Run the narrowest meaningful validation after edits, such as a focused Maven test, compile check, or behavior-specific command.
8. Report what changed, what validation ran, and any remaining blockers.

## Guardrails
- Do not invent new requirements.
- Do not add persistence, endpoints, models, dependencies, or workflows unless they are approved in the SDLC docs.
- Do not skip validation when a relevant command is available.
- Do not silently fix unrelated issues; mention them only if they block the approved task.
