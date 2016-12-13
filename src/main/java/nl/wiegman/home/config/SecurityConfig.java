//package nl.wiegman.home.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.builders.WebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//
//@Configuration
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().antMatchers("/static/**");
//    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        // Prevent the HTTP response header of "Pragma: no-cache".
//        http.headers().cacheControl().disable();
//    }
//}
