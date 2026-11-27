---
description: "Orchestrator agent for running the complete Bookshelf API Agentic SDLC workflow sequentially. Invokes requirements, architecture, design review, planning, implementation, code review, verification, and PR agents in order while preserving approval gates."
tools: [read, search, agent, mcp-atlassian/*]
agents: [requirements-agent, architect-agent, design-review, planner-agent, implementation-agent, code-review-agent, verify-agent, pr-agent]
---

# Agentic SDLC Orchestrator

## Input
- A user story, feature idea, change request, or request to resume the SDLC workflow.
- Any human approvals or answers required by the current workflow step.

## Skill
Load and follow `.github/skills/orchestrator-skill/SKILL.md`.

## Output
- Sequential delegation to every role agent from Step 1 through Step 8.
- A concise status update after each completed step.
- A stop-and-ask response at human approval gates or when a step is blocked or fails.
- A final summary linking the completed SDLC artifacts and reporting verification and PR readiness.
