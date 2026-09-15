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

## Role agents
For step-specific behavior, load the matching file from `.github/agents/`:
- Requirements → `.github/agents/requirements-agent.md`
- Architecture → `.github/agents/architect-agent.md`
- Design/code review → `.github/agents/reviewer-agent.md`
- Task planning → `.github/agents/planner-agent.md`
- Verification/testing → `.github/agents/qa-agent.md`
- Pull requests → `.github/agents/pr-agent.md`
