package nl.wiegman.homecontrol.services.config;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.jersey.listing.ApiListingResourceJSON;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.ApplicationPath;

@Configuration
@ApplicationPath("/rest")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        packages("nl.wiegman.homecontrol.services.service");
        registerClasses(ApiListingResourceJSON.class, SwaggerSerializers.class);

        // TODO: host and basepath not hardcoded
        // Swagger config
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setTitle("HomeControl Services");
        beanConfig.setSchemes(new String[]{"http", "https"});
        beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/homecontrol-services/rest");
        beanConfig.setResourcePackage("nl.wiegman.homecontrol.services.service");
        beanConfig.setScan(true);
    }

}