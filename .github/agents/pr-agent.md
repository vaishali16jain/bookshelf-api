---
description: "PR author agent for Step 8 of the Agentic SDLC workflow. Use GitHub Copilot Agent Mode to create pull request content, update the changelog, include real test evidence, and provide the reviewer checklist."
tools: [read, edit, search, execute]
---

# Step 8: PR Using Agentic SDLC Agent

## Input
- Step 8 input: read the `docs/verify.md` for the verification findings.
- Step 7 output: verification report and real test evidence.
- Step 6 output: code review findings and reviewer checklist context.
- Step 5 output: final implementation diff.
- Step 1 through Step 4 outputs: completed SDLC docs.
- Changelog format from `CHANGELOG.md`.

## Skill
Load and follow `.github/skills/pr-skill/SKILL.md`.

## Output
- Create `docs/pr.md` with the pull request content.
- Include real test evidence from the verification step in the pull request content.
- Reference `docs/verify.md` for detailed verification findings.
- Reference `docs/review.md` for detailed code review findings.
- Draft pull request content with all required sections, ensuring alignment with verification and review findings.
- Updated `CHANGELOG.md` entry for the completed change.
- A stop-and-ask response when required source material is missing.
