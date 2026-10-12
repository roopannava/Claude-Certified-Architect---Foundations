# marketsdata_backend

Spring Boot (Java 21) service for the market-dashboards demo. One endpoint takes
a prompt and lets Claude decide which dashboard(s) to show via a **tool-use
agent loop**. Each dashboard is a tool; Claude calls the ones the prompt asks
for.

Standalone — unrelated to any other project in this workspace.

## Run

```bash
export ANTHROPIC_API_KEY=sk-ant-...   # drives the agent loop
export FINNHUB_API_KEY=...            # live price / % change for the stock rows
./mvnw spring-boot:run                # starts on http://localhost:8080
```

(First run downloads Maven via the `./mvnw` wrapper — no system Maven needed.)

## Endpoint

`POST /api/dashboard`  — body `{ "prompt": "..." }`

Returns the echoed prompt plus one list per dashboard; a list is populated only
when Claude chose to call that dashboard's tool.

```bash
curl -X POST http://localhost:8080/api/dashboard \
  -H 'Content-Type: application/json' \
  -d '{"prompt":"show me the biggest losers and the most shorted stocks"}'
```

## Design

```
DashboardController  POST /api/dashboard
        │
        ▼
DashboardServiceImpl ──▶ tool-use loop ──▶ ServiceFactory.get(type) ──▶ DataService
   (Claude picks tools)                                                  ├─ MostShortedServiceImpl
                                                                         ├─ DayLosersServiceImpl
                                                                         └─ TopMutualFundsServiceImpl
```

- `DataService` — common interface; each provider returns `List<StockRow>`
  (symbol, name, price, change %, plus a signature metric).
- `ServiceFactory` — Spring injects every `DataService` and the factory indexes
  them by `ServiceType`, so callers ask for a provider by type.
- `DashboardServiceImpl` — the manual tool-use loop. It exposes the three
  providers as tools, sends the prompt to Claude, runs whichever tools Claude
  requests, feeds the results back, and repeats until Claude stops calling
  tools.
- `FinnhubClient` — wraps Finnhub's free quote endpoint for live price and
  day % change; degrades to a curated fallback price on any failure.

Each dashboard holds a curated set of 5 symbols (the "membership", which no free
API provides); price and % change are pulled live from Finnhub. The signature
metric (short % of float, avg volume, YTD return) is curated.
