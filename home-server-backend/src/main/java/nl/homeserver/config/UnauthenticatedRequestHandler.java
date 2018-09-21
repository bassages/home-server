package nl.homeserver.config;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Redirects all unauthenticated requests to the login page,
 * EXCEPT for api and websocket requests: they will be rejected with a 401 status.
 */
@Component
public class UnauthenticatedRequestHandler implements AuthenticationEntryPoint {

    private static final HttpStatus UNAUTHORIZED = HttpStatus.UNAUTHORIZED;

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) throws IOException {
        final String servletPath = request.getServletPath();
        if (servletPath.startsWith(Paths.API) || servletPath.startsWith(Paths.WEBSOCKET)) {
            response.sendError(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase());
        } else {
            response.sendRedirect(Paths.LOGIN);
        }
    }
}
