package nl.homeserver.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Redirects the user to root when a user is already logged in AND is requesting the login page.
 * We don't want users to see the login page when already logged in. Only logout is allowed then.
 */
public class AlreadyLoggedInUserInterceptor extends HandlerInterceptorAdapter {

    private static final boolean STOP_EXCECUTION_CHAIN = false;
    private static final boolean PROCEED_EXCECUTION_CHAIN = true;

    private static final String AUTHENTICATION_NAME_WHEN_NOT_LOGGED_IN = "anonymousUser";

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response, final Object handler) throws Exception {
        final boolean alreadyLoggedIn = isUserLoggedIn();

        final boolean loginPageRequested = request.getServletPath().startsWith(Paths.LOGIN);

        if (alreadyLoggedIn && loginPageRequested) {
            response.sendRedirect(Paths.ROOT);
            return STOP_EXCECUTION_CHAIN;
        }

        return PROCEED_EXCECUTION_CHAIN;
    }

    private static boolean isUserLoggedIn() {
        try {
            return !SecurityContextHolder.getContext().getAuthentication().getName().equals(AUTHENTICATION_NAME_WHEN_NOT_LOGGED_IN);
        } catch (final Exception e) {
            return false;
        }
    }
}
