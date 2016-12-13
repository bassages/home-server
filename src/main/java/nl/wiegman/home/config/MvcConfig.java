//package nl.wiegman.home.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.CacheControl;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
//
//import java.util.concurrent.TimeUnit;
//
//@Configuration
//public class MvcConfig extends WebMvcConfigurerAdapter {
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        // Resources without Spring Security. No cache control response headers.
//        registry.addResourceHandler("**/*.html")
//                .addResourceLocations("classpath:/static/")
//                .setCacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
//                .setCachePeriod(3600 * 24);
//        registry.addResourceHandler("**/*.js")
//                .addResourceLocations("classpath:/static/")
//                .setCacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
//                .setCachePeriod(3600 * 24);
//        registry.addResourceHandler("images/*.*")
//                .addResourceLocations("classpath:/static/")
//                .setCacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
//                .setCachePeriod(3600 * 24);
//        registry.addResourceHandler("**/*.css")
//                .addResourceLocations("classpath:/static/")
//                .setCacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
//                .setCachePeriod(3600 * 24);
//    }
//}