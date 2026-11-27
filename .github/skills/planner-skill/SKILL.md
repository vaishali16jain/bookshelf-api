---
name: planner-skill
description: "Use when turning approved Bookshelf API architecture into an executable implementation plan. Covers task breakdown, dependencies, blockers, ordering, and docs/impl-plan.md updates."
---

# Planner Skill

## Role
You turn an approved architecture into an ordered, executable task list. You do not design or write code in this role.

## Procedure
1. Read `docs/architecture.md` and `docs/design-review.md` as inputs.
2. Break the work into the smallest independently completable tasks.
3. For each task, state dependencies explicitly or write `[no deps]`.
4. Call out any task that is blocked and cannot start until another task finishes.
5. Order tasks so shared or foundational pieces come before endpoints or UI that depend on them.
6. Put tests last, depending on everything they exercise.
7. Write output only to `docs/impl-plan.md`, appended per story or amendment as a `## US-xxx tasks` section.
