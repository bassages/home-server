package nl.homeserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static org.springframework.http.HttpStatus.RESET_CONTENT;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${home-server.enable-ssl}")
    private boolean enableSsl;

    private final UnauthenticatedRequestHandler unauthenticatedRequestHandler;

    public WebSecurityConfig(final UnauthenticatedRequestHandler unauthenticatedRequestHandler) {
        this.unauthenticatedRequestHandler = unauthenticatedRequestHandler;
    }

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .headers().frameOptions().sameOrigin().and()
            .httpBasic().and()
            .logout()
                .addLogoutHandler((request, response, authentication) -> response.setStatus(RESET_CONTENT.value()))
                .invalidateHttpSession(true)
                .clearAuthentication(true).and()
            .authorizeRequests()
                .requestMatchers(EndpointRequest.to("status", "info")).permitAll()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .antMatchers("/", "/**/*.js", "/**/*.css", "/index.html", "/assets/**/*").permitAll()
                .antMatchers(Paths.LOGIN).permitAll()
                .anyRequest().authenticated().and()
                .exceptionHandling().authenticationEntryPoint(unauthenticatedRequestHandler);

        if (enableSsl) {
            http.requiresChannel().anyRequest().requiresSecure();
        }
    }
}
