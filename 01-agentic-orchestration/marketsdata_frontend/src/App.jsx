import { useState } from 'react'
import './App.css'

// The dashboard-specific metric (short %, volume, YTD return) rides along in
// each row as metricLabel/metricValue, so one table layout serves all three.
function DashboardTable({ title, rows }) {
  if (!rows?.length) return null
  const metricLabel = rows[0].metricLabel
  return (
    <section className="result-block">
      <h3>{title}</h3>
      <table>
        <thead>
          <tr>
            <th>Symbol</th>
            <th>Name</th>
            <th>Price ($)</th>
            <th>Change (%)</th>
            <th>{metricLabel}</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row, i) => (
            <tr key={i}>
              <td>{row.symbol}</td>
              <td>{row.name}</td>
              <td>{row.price.toFixed(2)}</td>
              <td className={row.changePercent < 0 ? 'down' : 'up'}>
                {row.changePercent.toFixed(2)}
              </td>
              <td>{row.metricValue}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}

export default function App() {
  const [prompt, setPrompt] = useState('')
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setLoading(true)
    setError('')
    setResult(null)
    try {
      const response = await fetch('/api/dashboard', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt }),
      })
      if (!response.ok) {
        throw new Error(`Backend returned ${response.status}`)
      }
      setResult(await response.json())
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="app">
      <h1>Market Dashboards</h1>

      <form onSubmit={handleSubmit}>
        <textarea
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="e.g. Show me the biggest day losers and the most shorted stocks"
          rows={4}
        />
        <button type="submit" disabled={loading || !prompt.trim()}>
          {loading ? 'Loading…' : 'Show Dashboards'}
        </button>
      </form>

      {error && <p className="error">Error: {error}</p>}

      {result && (
        <div className="results">
          <p className="echo">
            Prompt: <em>{result.prompt}</em>
          </p>
          <DashboardTable title="Most Shorted Stocks" rows={result.mostShorted} />
          <DashboardTable title="Day Losers" rows={result.dayLosers} />
          <DashboardTable title="Top Mutual Funds" rows={result.topMutualFunds} />
        </div>
      )}
    </main>
  )
}
