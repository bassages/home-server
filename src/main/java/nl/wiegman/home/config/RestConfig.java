package nl.wiegman.home.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.ApplicationPath;

@Configuration
@ApplicationPath("/rest")
public class RestConfig extends ResourceConfig {

    public RestConfig() {
        packages("nl.wiegman.home.service");
    }
}