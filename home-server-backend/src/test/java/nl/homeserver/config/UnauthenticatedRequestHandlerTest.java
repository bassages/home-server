package nl.homeserver.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    public void whenCommenceThenStatusUnauthorizedIsSetOnResponse() throws Exception {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final AuthenticationException error = mock(AuthenticationException.class);

        unauthenticatedRequestHandler.commence(request, response, error);

        verify(response).sendError(401, "Unauthorized");
    }
}