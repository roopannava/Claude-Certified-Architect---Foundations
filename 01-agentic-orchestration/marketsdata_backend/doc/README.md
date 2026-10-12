# Market Dashboards

A small full-stack demo: a React prompt box calls a Spring Boot backend that
runs a **Claude tool-use agent loop**. Each market dashboard is exposed to Claude
as a tool; the prompt decides which dashboards it shows.

**Stack:** Java 21 · Spring Boot 4.1.1 · React + Vite · Anthropic Java SDK ·
Finnhub · `POST /api/dashboard`

---

## What it does

You type a prompt — for example `Show me the biggest losers and the most shorted
stocks` — and hit **Show Dashboards**. Claude reads the prompt, calls the
dashboard tool(s) you asked for (one, two, or all three), and the matching
tables appear. Three dashboards are available:

- **Most Shorted Stocks** — heavily shorted names, with short % of float.
- **Day Losers** — the day's biggest decliners, with average volume.
- **Top Mutual Funds** — large index/active funds, with YTD return.

Each row is a `StockRow`: symbol, name, price, day change %, and the dashboard's
signature metric. Price and change % are pulled **live from Finnhub**; the
signature metric is curated.

---

## How it flows

One prompt can become several jobs. Claude decides which dashboard tools to
call, and the loop runs them:

```
prompt ─▶ Claude ─▶ tool_use: most_shorted ─┐
                    tool_use: day_losers    ├─▶ run each provider ─▶ results ─▶ Claude ─▶ done
                    (zero, one, or more)    ┘
```

This is the whole point of the demo: **how Claude orchestrates multiple tools
from a single prompt.**

---

## Design

A common `DataService` interface has three implementations, one per
`ServiceType` (`MOST_SHORTED`, `DAY_LOSERS`, `TOP_MUTUAL_FUNDS`). Spring injects
all of them into a `ServiceFactory`, which indexes them by type so a caller asks
for a provider by role rather than by class.

```
DashboardController  POST /api/dashboard
        │
        ▼
DashboardServiceImpl ──▶ tool-use loop ──▶ ServiceFactory.get(type) ──▶ DataService
                                                                        ├─ MostShortedServiceImpl
                                                                        ├─ DayLosersServiceImpl
                                                                        └─ TopMutualFundsServiceImpl
```

`DashboardServiceImpl` runs the manual tool-use loop: it exposes the three
providers as tools, sends the prompt, runs whichever tools Claude requests,
hands the results back, and repeats until Claude stops calling tools. Each
provider pulls live quotes through `FinnhubClient`, which falls back to a
curated price on any failure so a dashboard always renders.

---

## The data shape

Each provider returns a list of `StockRow` records. Example (values illustrative;
price/change come live from Finnhub):

| Dashboard        | Symbol | Name                | Price | Change % | Metric            |
|------------------|--------|---------------------|------:|---------:|-------------------|
| Most Shorted     | CVNA   | Carvana Co.         | live  | live     | Short % Float 38% |
| Day Losers       | SNAP   | Snap Inc.           | live  | live     | Avg Volume 38.1M  |
| Top Mutual Funds | VFIAX  | Vanguard 500 Admiral| ~NAV  | live/0   | YTD Return +18.4% |

---

## The endpoint

```bash
curl -X POST http://localhost:8080/api/dashboard \
  -H 'Content-Type: application/json' \
  -d '{"prompt":"show me the biggest losers and the most shorted stocks"}'
```

Response (trimmed):

```json
{
  "prompt": "show me the biggest losers and the most shorted stocks",
  "mostShorted":    [ /* 5 rows */ ],
  "dayLosers":      [ /* 5 rows */ ],
  "topMutualFunds": []
}
```

Only the dashboards Claude called are populated; the rest come back empty.

---

## Run it locally

1. **Backend** — starts on port 8080. Needs two keys in the environment.

   ```bash
   cd marketsdata_backend
   export ANTHROPIC_API_KEY=sk-ant-...
   export FINNHUB_API_KEY=...
   ./mvnw spring-boot:run
   ```

2. **Frontend** — starts on port 5173. Vite proxies `/api` to `:8080`, so the
   browser sees one origin and no CORS setup is required.

   ```bash
   cd marketsdata_frontend
   npm install
   npm run dev
   ```

3. Open `http://localhost:5173`, type a prompt, and click **Show Dashboards**.
   A table appears for each dashboard Claude chose.

---

_Standalone demo. Backend (`marketsdata_backend`): Spring Boot 4.1.1 on Java 21,
package `com.marketsdata`. Frontend (`marketsdata_frontend`): React + Vite._
