package com.marketsdata.service;

import com.marketsdata.model.StockRow;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Day-losers dashboard: a curated set of high-volume, volatile names that often
 * show up among the day's biggest decliners. The live day change from Finnhub
 * is the signal here; the average-volume figure is curated.
 */
@Service
public class DayLosersServiceImpl implements DataService {

    private static final String METRIC = "Avg Volume";

    private final FinnhubClient finnhub;

    public DayLosersServiceImpl(FinnhubClient finnhub) {
        this.finnhub = finnhub;
    }

    @Override
    public ServiceType type() {
        return ServiceType.DAY_LOSERS;
    }

    @Override
    public List<StockRow> create() {
        return List.of(
                row("SNAP", "Snap Inc.", 9.80, "38.1M"),
                row("PLUG", "Plug Power Inc.", 2.10, "55.4M"),
                row("LCID", "Lucid Group, Inc.", 3.00, "72.0M"),
                row("RIVN", "Rivian Automotive, Inc.", 13.5, "41.6M"),
                row("NIO", "NIO Inc.", 4.70, "60.2M")
        );
    }

    private StockRow row(String symbol, String name, double fallbackPrice, String avgVolume) {
        FinnhubClient.Quote q = finnhub.quote(symbol, fallbackPrice);
        return new StockRow(symbol, name, q.price(), q.changePercent(), METRIC, avgVolume);
    }
}
