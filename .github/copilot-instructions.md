# Repo-wide Copilot Instructions

This repo is a Java 17 / Spring Boot prototype used for an Agentic SDLC
capstone exercise. Applies to every Copilot Chat / Agent Mode session here.

## General rules
- This is a learning project — favor clear, idiomatic Java over clever code.
- Do not add persistence (database, files) unless a requirement in
  `docs/requirements.md` explicitly calls for it.
- Every new endpoint must have a corresponding entry in `docs/requirements.md`
  before implementation begins.
- Never invent requirements — if something is ambiguous, ask.
- Keep `docs/architecture.md`, `docs/design-review.md`, and
  `docs/impl-plan.md` in sync with the code. If you change a route or model,
  say so and offer to update the relevant doc.

## Role agents and skills
For step-specific behavior, load the matching wrapper from `.github/agents/`.
Each wrapper declares its input, output, and the skill in `.github/skills/`
that contains the detailed role instructions:
- Full workflow: Orchestrator → `.github/agents/orchestrator-agent.md` → `.github/skills/orchestrator-skill/SKILL.md`
- Step 1: Requirements → `.github/agents/requirements-agent.md` → `.github/skills/requirements-skill/SKILL.md`
- Step 2: Architecture → `.github/agents/architect-agent.md` → `.github/skills/architecture-skill/SKILL.md`
- Step 3: Design Review → `.github/agents/design-review.md` → `.github/skills/design-review-skill/SKILL.md`
- Step 4: Implementation Planning → `.github/agents/planner-agent.md` → `.github/skills/planner-skill/SKILL.md`
- Step 5: Implementation → `.github/agents/implementation-agent.md` → `.github/skills/implementation-skill/SKILL.md`
- Step 6: Review → `.github/agents/code-review-agent.md` → `.github/skills/code-review-skill/SKILL.md`
- Step 7: Verify → `.github/agents/verify-agent.md` → `.github/skills/verify-skill/SKILL.md`
- Step 8: PR Using Agentic SDLC → `.github/agents/pr-agent.md` → `.github/skills/pr-skill/SKILL.md`
