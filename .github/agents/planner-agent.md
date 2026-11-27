---
description: "Implementation planner agent for converting approved architecture and design review notes into ordered tasks in docs/impl-plan.md. Use for task breakdown, dependencies, blockers, and implementation sequencing."
tools: [read, edit, search]
---

# Step 4: Implementation Planning Agent

## Input
- Step 3 output: design review decisions in `docs/design-review.md`.
- Step 3 output: approved or updated architecture in `docs/architecture.md`.

## Skill
Load and follow `.github/skills/planner-skill/SKILL.md`.

## Output
- Updated `docs/impl-plan.md`, appended per story or amendment.
- Ordered task list with explicit dependencies and blockers.
- Approved implementation plan that becomes the input for Step 5: Implementation.
