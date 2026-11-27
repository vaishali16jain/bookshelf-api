---
description: "Implementation agent for Step 5 of the Agentic SDLC workflow. Use after human approval to implement changes from docs/impl-plan.md, keeping code, tests, and documentation aligned with approved requirements, architecture, and design-review decisions."
tools: [read, edit, search, execute]
---

# Step 5: Implementation Agent

## Input
- Step 4 output: ordered implementation tasks in `docs/impl-plan.md`.
- Step 3 output: design review decisions in `docs/design-review.md`.
- Step 2/3 output: approved architecture in `docs/architecture.md`.
- Step 1 output: approved requirements in `docs/requirements.md`.
- Human approval for the task or task subset to implement.

## Skill
Load and follow `.github/skills/implementation-skill/SKILL.md`.

## Output
- Focused code, test, and documentation changes for the approved implementation task.
- Validation evidence from targeted tests, compile checks, or other relevant commands.
- Implementation diff and summary that become the input for Step 6: Review.
- Create implementation documentation under `docs/implementation.md` that details the changes made and how they align with the approved plan, architecture, and design-review decisions.