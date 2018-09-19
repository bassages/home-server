package nl.homeserver.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

@Component
public class SuccessfulLoginHandler extends SimpleUrlAuthenticationSuccessHandler {

    private RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(
            final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws ServletException, IOException {
        final SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (savedRequest == null) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        clearAuthenticationAttributes(request);

        final String targetUrl = savedRequest.getRedirectUrl();

        if (targetUrl.contains(Paths.ACTUATOR + "/")) {
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            getRedirectStrategy().sendRedirect(request, response, Paths.ROOT);
        }
    }

    public void setRequestCache(final RequestCache requestCache) {
        this.requestCache = requestCache;
    }
}
