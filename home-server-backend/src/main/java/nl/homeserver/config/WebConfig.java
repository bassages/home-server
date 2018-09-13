package nl.homeserver.config;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.http.CacheControl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebSecurity
public class WebConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
            "classpath:/META-INF/resources/",
            "classpath:/resources/",
            "classpath:/static/",
            "classpath:/public/"
    };

    private static final String LOGIN_PATH = DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL;

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .headers().frameOptions().sameOrigin().and()
            .httpBasic().and()
            .formLogin()
                .loginPage(LOGIN_PATH)
                .permitAll().and()
            .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true).and()
            .authorizeRequests()
                .requestMatchers(EndpointRequest.to("status", "info")).permitAll()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .antMatchers(LOGIN_PATH).permitAll()
                .anyRequest().authenticated()
        ;
    }

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController(LOGIN_PATH).setViewName("login");
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/index.html")
                .addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS)
                .setCacheControl(CacheControl.noStore());

        registry.addResourceHandler("/**")
                .addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS)
                .setCachePeriod((int) TimeUnit.DAYS.toSeconds(14L));
    }
}