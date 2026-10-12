package com.marketsdata.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Hands out a {@link DataService} by {@link ServiceType}.
 *
 * Spring injects every DataService bean into the constructor; the factory
 * indexes them by their {@code type()} so callers (the dashboard service) ask
 * for a provider by type instead of wiring each concrete class directly. Add a
 * new provider and it registers itself -- nothing here changes.
 */
@Component
public class ServiceFactory {

    private final Map<ServiceType, DataService> services;

    public ServiceFactory(List<DataService> providers) {
        this.services = providers.stream()
                .collect(Collectors.toMap(DataService::type, Function.identity()));
    }

    public DataService get(ServiceType type) {
        DataService service = services.get(type);
        if (service == null) {
            throw new IllegalArgumentException("No DataService registered for " + type);
        }
        return service;
    }
}
