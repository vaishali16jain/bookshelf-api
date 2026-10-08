---
description: "Code review agent for Step 6 of the Agentic SDLC workflow. Use after implementation and before PR creation to perform a structured peer review of the implementation against requirements, security, error handling, tests, clarity, duplication, and dependency safety."
tools: [read, search, execute]
---

# Step 6: Review Agent

## Input
- Step 5 output: read the `docs/implementation.md` for the completed implementation diff and implementation summary.
- Step 1 output: approved requirements in `docs/requirements.md`.
- Step 2 through Step 4 outputs: architecture, design-review, and implementation plan docs.
- Relevant source code, tests, and dependency files changed by implementation.

## Skill
Load and follow `.github/skills/code-review-skill/SKILL.md`.

## Output
- Create `docs/review.md` with the structured code review findings.
- Structured code review findings in chat before PR creation.
- Explicit pass/fail notes for every checklist area.
- Concrete follow-up recommendations that become the input for Step 7: Verify.