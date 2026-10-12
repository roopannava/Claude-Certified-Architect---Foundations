# marketsdata_frontend

React + Vite (JavaScript). A prompt box and a Submit button that call the
Spring Boot backend's `POST /api/dashboard` and render whichever market
dashboards Claude chose.

Standalone — unrelated to any other project in this workspace.

## Run

Start the backend first (see `../marketsdata_backend`), then:

```bash
npm install
npm run dev        # http://localhost:5173
```

`vite.config.js` proxies `/api` to `http://localhost:8080`, so the browser sees
one origin and no CORS setup is needed. Type a prompt, click **Show
Dashboards**, and a table appears for each dashboard Claude decided to show
(Most Shorted Stocks, Day Losers, Top Mutual Funds).

## Build

```bash
npm run build      # -> dist/
```
