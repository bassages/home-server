package nl.homeserver.config;

import lombok.NoArgsConstructor;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Paths {
    public static final String LOGIN = DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL;
    public static final String API = "/api";
}
