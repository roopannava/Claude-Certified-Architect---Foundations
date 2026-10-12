package com.marketsdata.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Thin wrapper over Finnhub's free quote endpoint:
 * {@code GET /api/v1/quote?symbol=SYM&token=KEY}.
 *
 * The key is read from the {@code FINNHUB_API_KEY} environment variable, never
 * hard-coded. Every failure mode -- missing key, non-200, network error, or a
 * symbol Finnhub can't price (some mutual funds return c=0) -- degrades to the
 * caller's curated fallback price so a dashboard always renders.
 */
@Component
public class FinnhubClient {

    private static final String QUOTE_URL = "https://finnhub.io/api/v1/quote";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey = System.getenv("FINNHUB_API_KEY");

    public Quote quote(String symbol, double fallbackPrice) {
        if (apiKey == null || apiKey.isBlank()) {
            return new Quote(fallbackPrice, 0.0);
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(QUOTE_URL + "?symbol=" + symbol + "&token=" + apiKey))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return new Quote(fallbackPrice, 0.0);
            }
            JsonNode root = mapper.readTree(response.body());
            double price = root.path("c").asDouble(0.0);
            double changePercent = root.path("dp").asDouble(0.0);
            // Finnhub returns c=0 for symbols it can't quote; keep the fallback price.
            if (price <= 0.0) {
                return new Quote(fallbackPrice, changePercent);
            }
            return new Quote(price, changePercent);
        } catch (Exception e) {
            return new Quote(fallbackPrice, 0.0);
        }
    }

    /** Live price and day percent change for one symbol. */
    public record Quote(double price, double changePercent) {
    }
}
