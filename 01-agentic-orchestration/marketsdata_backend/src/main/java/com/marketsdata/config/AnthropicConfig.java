package com.marketsdata.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes a single {@link AnthropicClient} for the whole app.
 *
 * {@code fromEnv()} reads the API key from the {@code ANTHROPIC_API_KEY}
 * environment variable (the SDK's default credential resolution), so no key is
 * ever hard-coded here. The client is thread-safe and meant to be shared, which
 * is exactly what a Spring singleton bean gives us.
 */
@Configuration
public class AnthropicConfig {

    @Bean
    public AnthropicClient anthropicClient() {
        return AnthropicOkHttpClient.fromEnv();
    }
}
