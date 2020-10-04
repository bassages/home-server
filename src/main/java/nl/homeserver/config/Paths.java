package nl.homeserver.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Paths {
    public static final String LOGIN = DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL;
    public static final String API = "/api";
}
