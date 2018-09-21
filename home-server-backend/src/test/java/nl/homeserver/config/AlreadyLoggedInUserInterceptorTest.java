package nl.homeserver.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class AlreadyLoggedInUserInterceptorTest {

    private final AlreadyLoggedInUserInterceptor alreadyLoggedInUserInterceptor = new AlreadyLoggedInUserInterceptor();

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Object handler;

    @Test
    public void givenUserAlreadyLoggedAndRequestsLoginPageInWhenPreHandleThenRedirectedToRoot() throws Exception {
        final Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("foobar");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(request.getServletPath()).thenReturn("/login");

        final boolean proceedExcecutionChain = alreadyLoggedInUserInterceptor.preHandle(request, response, handler);

        assertThat(proceedExcecutionChain).isFalse();
        verify(response).sendRedirect("/");
    }

    @Test
    public void givenUserNotLoggedAndRequestsLoginPageInWhenPreHandleThenProceedExcecutionChain() throws Exception {
        final Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("anonymousUser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(request.getServletPath()).thenReturn("/login");

        final boolean proceedExcecutionChain = alreadyLoggedInUserInterceptor.preHandle(request, response, handler);

        assertThat(proceedExcecutionChain).isTrue();
        verifyZeroInteractions(response);
    }

    @Test
    public void givenUserAlreadyLoggedAndNotRequestsLoginPageInWhenPreHandleThenProceedExcecutionChain() throws Exception {
        final Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("foobar");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(request.getServletPath()).thenReturn("/some-fancy-page");

        final boolean proceedExcecutionChain = alreadyLoggedInUserInterceptor.preHandle(request, response, handler);

        assertThat(proceedExcecutionChain).isTrue();
        verifyZeroInteractions(response);
    }

    @Test
    public void givenUserNotLoggedAndNotRequestsLoginPageInWhenPreHandleThenProceedExcecutionChain() throws Exception {
        final Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("anonymousUser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(request.getServletPath()).thenReturn("/some-fancy-page");

        final boolean proceedExcecutionChain = alreadyLoggedInUserInterceptor.preHandle(request, response, handler);

        assertThat(proceedExcecutionChain).isTrue();
        verifyZeroInteractions(response);
    }
}