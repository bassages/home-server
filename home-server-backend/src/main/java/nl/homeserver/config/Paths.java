package nl.homeserver.config;

import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;

public final class Paths {

    public static final String LOGIN = DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL;
    public static final String API = "/api";
    public static final String ROOT = "/";
    public static final String ACTUATOR = "/actuator";
}
