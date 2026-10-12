package com.marketsdata.service;

import com.marketsdata.model.StockRow;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Most-shorted-stocks dashboard: a curated set of well-known heavily-shorted
 * names. Price and day change are pulled live from Finnhub; the short-interest
 * figure is curated (not on Finnhub's free tier). Fallback prices keep the
 * dashboard populated when the live quote is unavailable.
 */
@Service
public class MostShortedServiceImpl implements DataService {

    private static final String METRIC = "Short % of Float";

    private final FinnhubClient finnhub;

    public MostShortedServiceImpl(FinnhubClient finnhub) {
        this.finnhub = finnhub;
    }

    @Override
    public ServiceType type() {
        return ServiceType.MOST_SHORTED;
    }

    @Override
    public List<StockRow> create() {
        return List.of(
                row("CVNA", "Carvana Co.", 265.0, "38.2%"),
                row("BYND", "Beyond Meat, Inc.", 6.50, "41.5%"),
                row("UPST", "Upstart Holdings, Inc.", 62.0, "33.1%"),
                row("GME", "GameStop Corp.", 24.0, "22.7%"),
                row("FUBO", "fuboTV Inc.", 3.40, "28.9%")
        );
    }

    private StockRow row(String symbol, String name, double fallbackPrice, String shortPctOfFloat) {
        FinnhubClient.Quote q = finnhub.quote(symbol, fallbackPrice);
        return new StockRow(symbol, name, q.price(), q.changePercent(), METRIC, shortPctOfFloat);
    }
}
