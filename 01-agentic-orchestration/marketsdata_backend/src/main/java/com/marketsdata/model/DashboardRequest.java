package com.marketsdata.model;

/**
 * The request body for POST /api/dashboard: just the prompt typed in the React
 * frontend, e.g. "show me the biggest losers and the most shorted stocks". The
 * prompt drives which dashboard tool(s) Claude calls.
 */
public record DashboardRequest(String prompt) {
}
