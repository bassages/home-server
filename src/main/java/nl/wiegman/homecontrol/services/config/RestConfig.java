package nl.wiegman.homecontrol.services.config;

import com.jcabi.manifests.Manifests;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.jersey.listing.ApiListingResourceJSON;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.ApplicationPath;

@Configuration
@ApplicationPath("/rest")
public class RestConfig extends ResourceConfig {

    public RestConfig() {
        packages("nl.wiegman.homecontrol.services.service", "nl.wiegman.homecontrol.services.config.converters");

        // Swagger config, see https://github.com/swagger-api/swagger-core/wiki/Swagger-Core-Jersey-2.X-Project-Setup#using-swaggers-beanconfig
        registerClasses(ApiListingResourceJSON.class, SwaggerSerializers.class);

        String version = "Unknown (not running in jar/war?)";
        String title = "HomeControl";
        if (title.equals(Manifests.read("Implementation-Title"))) {
            version = Manifests.read("Implementation-Version");
        }

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion(version);
        beanConfig.setPrettyPrint(true);
        beanConfig.setTitle(title);
        beanConfig.setSchemes(new String[]{"http", "https"});
        beanConfig.setBasePath("/homecontrol/rest");
        beanConfig.setResourcePackage("nl.wiegman.homecontrol.services.service");
        beanConfig.setScan(true);
    }
}