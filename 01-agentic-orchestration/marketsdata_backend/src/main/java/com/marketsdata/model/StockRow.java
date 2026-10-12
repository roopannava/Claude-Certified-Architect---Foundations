package com.marketsdata.model;

/**
 * One row in a market dashboard.
 *
 * {@code price} and {@code changePercent} are live values pulled from Finnhub's
 * quote endpoint. {@code metricLabel}/{@code metricValue} carry the dashboard's
 * signature figure (e.g. "Short % of Float" -> "38.2%") -- these are curated,
 * because Finnhub's free tier does not expose short interest or fund returns.
 */
public record StockRow(
        String symbol,
        String name,
        double price,
        double changePercent,
        String metricLabel,
        String metricValue
) {
}
