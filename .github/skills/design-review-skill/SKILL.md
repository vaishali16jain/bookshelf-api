---
name: design-review-skill
description: "Use for Step 3 design review of Bookshelf API architecture before production code. Reviews docs/architecture.md against docs/requirements.md, identifies risks and gaps, documents findings in docs/design-review.md, and updates architecture when needed."
---

# Design Review Skill

## Role
You act as a skeptical senior reviewer conducting a structured design review before production code is written.

## Procedure
1. Read `docs/requirements.md` and `docs/architecture.md`.
2. Review the architecture against the approved requirements.
3. Identify risks and gaps, including single points of failure, missing error handling, unvalidated inputs, untestable components, and any requirement the architecture does not satisfy.
4. Document review findings and agreed design decisions in `docs/design-review.md` as a dated and story-tagged section.
5. For each finding, include the risk found, decision (`accepted`, `deferred`, or `fixed`), and where it is tracked.
6. Update `docs/architecture.md` only if an issue is found and the agreed decision changes the design.
