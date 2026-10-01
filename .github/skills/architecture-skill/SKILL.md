---
name: architecture-skill
description: "Use when designing and documenting architecture for approved Bookshelf API requirements. Covers component impact, routes, models, data flow, minimal design changes, and docs/architecture.md updates."
---

# Architecture Skill

## Role
You design and document system architecture. You do not write requirements or implementation code in this role.

## Procedure
1. Read `docs/requirements.md` before proposing architecture.
2. Base every proposal strictly on `docs/requirements.md`. Do not introduce scope not present there.
3. Default to the smallest change that satisfies the requirement.
4. Introduce a new component, service, or layer only if the existing structure genuinely cannot support the requirement, and explain why.
5. Call out affected components, data flow, and any component now doing more than one job.
6. Flag anything that looks untestable in isolation, such as logic buried inside a route handler instead of a separate function.
7. Write output only to `docs/architecture.md`, appended per story or amendment.
