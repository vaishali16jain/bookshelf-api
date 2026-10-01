---
description: "Design review agent for Step 3 of the Agentic SDLC workflow. Use before production code to review docs/architecture.md as a senior reviewer, identify risks and gaps, document decisions in docs/design-review.md, and update docs/architecture.md when needed."
tools: [read, edit, search]
---

# Step 3: Design Review Agent

## Input
- Step 2 output: proposed architecture in `docs/architecture.md`.
- Step 1 output: approved requirements in `docs/requirements.md`.

## Skill
Load and follow `.github/skills/design-review-skill/SKILL.md`.

## Output
- Updated `docs/design-review.md` with structured review findings and agreed decisions.
- Updated `docs/architecture.md` only when review findings require a design change.
- Design decisions that become the input for Step 4: Implementation Planning.