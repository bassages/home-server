package nl.wiegman.home.config;

import nl.wiegman.home.service.api.*;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.ApplicationPath;

@Configuration
@ApplicationPath("/rest")
public class RestConfig extends ResourceConfig {

    public RestConfig() {
        register(CacheServiceRest.class);
        register(GasServiceRest.class);
        register(KlimaatServiceRest.class);
        register(EnergiecontractServiceRest.class);
        register(MeterstandServiceRest.class);
        register(MindergasnlSettingsServiceRest.class);
        register(StroomServiceRest.class);

        // See https://github.com/jersey/jersey/pull/196
        // packages("nl.wiegman.home.service");
    }
}