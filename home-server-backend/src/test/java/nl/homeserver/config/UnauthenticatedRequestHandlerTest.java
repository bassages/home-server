package nl.homeserver.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.AuthenticationException;

@RunWith(MockitoJUnitRunner.class)
public class UnauthenticatedRequestHandlerTest {

    @InjectMocks
    private UnauthenticatedRequestHandler unauthenticatedRequestHandler;

    @Test
    public void givenRequestToApiPathWhenCommenceThenStatusUnauthorizedIsSetOnResponse() throws Exception {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final AuthenticationException error = mock(AuthenticationException.class);

        when(request.getServletPath()).thenReturn("/api/some-sub-path");

        unauthenticatedRequestHandler.commence(request, response, error);

        verify(response).setStatus(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void givenRequestToNotApiPathWhenCommenceThenRedirectedToLogin() throws Exception {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final AuthenticationException error = mock(AuthenticationException.class);

        when(request.getServletPath()).thenReturn("/some-path");

        unauthenticatedRequestHandler.commence(request, response, error);

        verify(response).sendRedirect("/login");
    }
}