package nl.homeserver.config;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Reject all unauthenticated requests with a 401 status.
 */
@Component
public class UnauthenticatedRequestHandler implements AuthenticationEntryPoint {

    private static final HttpStatus UNAUTHORIZED = HttpStatus.UNAUTHORIZED;

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) throws IOException {
        response.sendError(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase());
    }
}
