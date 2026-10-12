package com.marketsdata.service;

/**
 * The dashboards the factory can hand out. Each maps to one tool exposed to
 * Claude; the enum name, lower-cased, is the tool name (MOST_SHORTED ->
 * "most_shorted"), so the agent loop can turn a tool name back into a type.
 */
public enum ServiceType {
    MOST_SHORTED,
    DAY_LOSERS,
    TOP_MUTUAL_FUNDS
}
