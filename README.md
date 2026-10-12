# Claude Certified Architect – Foundations

Hands-on examples built while preparing for the **Claude Certified Architect –
Foundations (CCAR-F)** certification. Each example is a small, self-contained
full-stack app that exercises one or more exam domains with working code rather
than theory.

## Exam domains

| Domain | Weight |
|--------|-------:|
| Agentic Architecture & Orchestration | 27% |
| Claude Code Configuration & Workflows | 20% |
| Prompt Engineering & Structured Output | 20% |
| Tool Design & MCP Integration | 18% |
| Context Management & Reliability | 15% |

## Examples

| # | Example | Focus | Primary domain(s) |
|---|---------|-------|-------------------|
| 01 | [Agentic Orchestration](01-agentic-orchestration/) | One prompt → Claude calls one or more market-dashboard tools, over live Finnhub data, via a hand-written tool-use loop | Agentic Architecture & Orchestration · Tool Design |

### 01 — Agentic Orchestration

A React prompt box calls a Spring Boot backend that runs a **manual Claude
tool-use loop**. Three market dashboards (most shorted, day losers, top mutual
funds) are each exposed to Claude as a tool; the prompt decides which it calls.
Price and day change come live from Finnhub.

- **Backend:** `01-agentic-orchestration/marketsdata_backend` — Java 21 · Spring
  Boot 4.1.1 · Anthropic Java SDK · `POST /api/dashboard`
- **Frontend:** `01-agentic-orchestration/marketsdata_frontend` — React + Vite

See each project's README to run it (both an `ANTHROPIC_API_KEY` and a
`FINNHUB_API_KEY` are required for the backend).

---

_A learning portfolio. Not affiliated with Anthropic; "Claude Certified
Architect – Foundations" refers to the certification these examples study for._
