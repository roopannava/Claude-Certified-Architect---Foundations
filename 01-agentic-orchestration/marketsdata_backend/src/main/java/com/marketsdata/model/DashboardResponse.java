package com.marketsdata.model;

import java.util.List;

/**
 * What POST /api/dashboard returns: the echoed prompt plus one list per
 * dashboard. A list is populated only when Claude chose to call that dashboard's
 * tool -- the others come back empty, which is how the UI knows what to show.
 */
public record DashboardResponse(
        String prompt,
        List<StockRow> mostShorted,
        List<StockRow> dayLosers,
        List<StockRow> topMutualFunds
) {
}
