package com.marketsdata.service;

import com.marketsdata.model.StockRow;

import java.util.List;

/**
 * A provider of one dashboard's rows. The three implementations -- most
 * shorted, day losers, top mutual funds -- each produce a list of
 * {@link StockRow}s.
 *
 * {@code type()} lets the {@link ServiceFactory} index the implementations, so
 * callers ask for a provider by {@link ServiceType} rather than depending on a
 * concrete class.
 */
public interface DataService {

    ServiceType type();

    List<StockRow> create();
}
