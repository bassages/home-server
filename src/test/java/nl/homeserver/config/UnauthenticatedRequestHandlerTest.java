package nl.homeserver.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class UnauthenticatedRequestHandlerTest {

    @InjectMocks
    UnauthenticatedRequestHandler unauthenticatedRequestHandler;

    @Test
    void whenCommenceThenStatusUnauthorizedIsSetOnResponse() throws Exception {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final AuthenticationException error = mock(AuthenticationException.class);

        unauthenticatedRequestHandler.commence(request, response, error);

        verify(response).sendError(401, "Unauthorized");
    }
}
