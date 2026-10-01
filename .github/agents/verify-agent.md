---
description: "Verify agent for Step 7 of the Agentic SDLC workflow. Use after code review and before PR creation to generate and run a comprehensive verification suite covering unit tests, integration tests, functional checks, and final output document quality."
tools: [read, edit, search, execute]
---

# Step 7: Verify Agent

## Input
- Read `src/test` for existing test cases, coverage, and implementation details.
- Step 6 output: read code review findings and follow-up recommendations.
- Step 5 output: completed implementation diff and validation evidence.
- Step 1 through Step 4 outputs: requirements, architecture, design review, and implementation plan docs.
- Source code, tests, and final output documents that need content quality checks.

## Skill
Load and follow `.github/skills/verify-skill/SKILL.md`.

## Output
- Create `docs/verify.md` with the verification findings.
- Comprehensive verification suite additions or updates when coverage is missing.
- Real execution evidence for unit, integration, and relevant functional checks.
- Final output document quality findings.
- Clear pass/fail verification report that becomes the input for Step 8: PR Using Agentic SDLC.