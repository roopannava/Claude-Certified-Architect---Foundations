package com.marketsdata.service;

import com.marketsdata.model.StockRow;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Top-mutual-funds dashboard: a curated set of large, well-known index and
 * active funds. Finnhub's free quote endpoint often can't price mutual funds
 * (it returns c=0), so the curated NAV fallback usually stands in for price;
 * the YTD-return figure is curated.
 */
@Service
public class TopMutualFundsServiceImpl implements DataService {

    private static final String METRIC = "YTD Return";

    private final FinnhubClient finnhub;

    public TopMutualFundsServiceImpl(FinnhubClient finnhub) {
        this.finnhub = finnhub;
    }

    @Override
    public ServiceType type() {
        return ServiceType.TOP_MUTUAL_FUNDS;
    }

    @Override
    public List<StockRow> create() {
        return List.of(
                row("VFIAX", "Vanguard 500 Index Admiral", 520.0, "+18.4%"),
                row("FXAIX", "Fidelity 500 Index Fund", 195.0, "+18.3%"),
                row("VTSAX", "Vanguard Total Stock Mkt Admiral", 135.0, "+17.1%"),
                row("SWPPX", "Schwab S&P 500 Index Fund", 82.0, "+18.2%"),
                row("FCNTX", "Fidelity Contrafund", 20.0, "+21.5%")
        );
    }

    private StockRow row(String symbol, String name, double fallbackNav, String ytdReturn) {
        FinnhubClient.Quote q = finnhub.quote(symbol, fallbackNav);
        return new StockRow(symbol, name, q.price(), q.changePercent(), METRIC, ytdReturn);
    }
}
