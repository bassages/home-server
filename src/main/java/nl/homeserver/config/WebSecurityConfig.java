package nl.homeserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpStatus.RESET_CONTENT;

@Configuration
public class WebSecurityConfig {

    @Value("${home-server.enable-ssl}")
    private boolean enableSsl;

    private final UnauthenticatedRequestHandler unauthenticatedRequestHandler;

    public WebSecurityConfig(final UnauthenticatedRequestHandler unauthenticatedRequestHandler) {
        this.unauthenticatedRequestHandler = unauthenticatedRequestHandler;
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .headers().frameOptions().sameOrigin().and()
            .httpBasic().and()
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            )
            .logout()
            .addLogoutHandler((request, response, authentication) -> response.setStatus(RESET_CONTENT.value()))
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID").and()
            .authorizeHttpRequests()
            .requestMatchers(EndpointRequest.to("status", "info")).permitAll()
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
            .requestMatchers("/", "/*.html", "/*.js", "/*.css", "/assets/**").permitAll()
            .requestMatchers(Paths.LOGIN).permitAll()
            .anyRequest().authenticated().and()
            .exceptionHandling().authenticationEntryPoint(unauthenticatedRequestHandler);

        if (enableSsl) {
            http.requiresChannel().anyRequest().requiresSecure();
        }
        return http.build();
    }
}
