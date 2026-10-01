---
name: pr-skill
description: "Use for Step 8 PR creation in the Bookshelf API SDLC workflow. Uses Copilot Agent Mode to draft the PR description, update CHANGELOG.md, include real test evidence, list known limitations, and create the reviewer checklist."
---

# PR Skill

## Role
You close out the SDLC cycle by creating the pull request materials. You do not change production code or requirements in this role. Summarize what already happened in docs and in the diff, and update the changelog entry for the completed change.

## Procedure
1. Read the final implementation diff and relevant SDLC docs.
2. Read `CHANGELOG.md` and follow its existing format.
3. Confirm real test evidence exists from the verify step or CI results.
4. If any required source material is missing, stop and ask rather than inventing content.
5. Add or update the `CHANGELOG.md` entry for the completed change.
6. Draft the PR description with all required sections.

## Required PR Sections
1. **Summary** - 2-3 sentences describing what was built and why.
2. **Changes Made** - bulleted list of files added or modified and the reason for each. Pull this from the actual diff, not memory.
3. **Test Evidence** - paste the real test run output from the verify step or link to CI results. Never fabricate or paraphrase test results.
4. **Known Limitations** - anything left not found, deferred, or explicitly out of scope per `docs/requirements.md` or `docs/design-review.md`.
5. **Reviewer Checklist** - a tick-list the human reviewer must complete, covering correctness, security, error handling, test coverage, code clarity, duplication, and dependency safety.

## Changelog Entry
- Update `CHANGELOG.md` using the existing project format.
- Include the user-visible change, relevant SDLC artifact additions, and any verification evidence that belongs in the changelog.
- Do not invent release numbers, dates, or results that are not present in the repo or supplied by the human.
