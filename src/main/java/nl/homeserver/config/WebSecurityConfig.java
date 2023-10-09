package nl.homeserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.http.HttpStatus.RESET_CONTENT;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class WebSecurityConfig {

    @Value("${home-server.enable-ssl}")
    private boolean enableSsl;

    private final UnauthenticatedRequestHandler unauthenticatedRequestHandler;

    public WebSecurityConfig(final UnauthenticatedRequestHandler unauthenticatedRequestHandler) {
        this.unauthenticatedRequestHandler = unauthenticatedRequestHandler;
    }

    @SuppressWarnings("Convert2MethodRef")
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers ->
                headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            .httpBasic(withDefaults())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            )
            .logout(logout -> logout
                .addLogoutHandler((request, response, authentication) -> response.setStatus(RESET_CONTENT.value()))
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
            )
            .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                    .requestMatchers(EndpointRequest.to("status", "info")).permitAll()
                    .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                    .requestMatchers(
                            new AntPathRequestMatcher("/"),
                            new AntPathRequestMatcher("/*.html"),
                            new AntPathRequestMatcher("/*.js"),
                            new AntPathRequestMatcher("/*.css"),
                            new AntPathRequestMatcher("/assets/**")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher(Paths.LOGIN)).permitAll()
                    .anyRequest().authenticated()
            )
            .rememberMe(rememberMe -> rememberMe.alwaysRemember(true))
            .exceptionHandling(exceptionHandling ->
                exceptionHandling.authenticationEntryPoint(unauthenticatedRequestHandler)
            );

        if (enableSsl) {
            http.requiresChannel(requiresChannel -> requiresChannel.anyRequest().requiresSecure());
        }
        return http.build();
    }
}
