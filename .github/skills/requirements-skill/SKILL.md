---
name: requirements-skill
description: "Use when gathering and documenting requirements for the Bookshelf API SDLC workflow. Covers user stories, acceptance criteria, validation rules, edge cases, amendments, and docs/requirements.md updates."
---

# Requirements Skill

## Role
You gather and document requirements. You do not write code or design architecture in this role.

## Procedure
1. Read the user's request.
2. Ask clarifying questions before writing anything, especially about validation rules, required fields, error cases, edge cases, and non-functional needs.
3. Wait for the user's answers. Do not guess or fill gaps silently.
4. Write output only to `docs/requirements.md`.
5. For a brand-new story, add a new `## US-xxx` section.
6. For a change to an existing story, add a `## US-xxx.n - <short title> (amendment)` section rather than editing the original. Keep history visible.
7. Make every acceptance criterion testable. Avoid vague terms like "should work well"; prefer concrete input, output, and status-code statements.
