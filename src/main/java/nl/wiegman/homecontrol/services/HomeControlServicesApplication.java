package nl.wiegman.homecontrol.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class HomeControlServicesApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(HomeControlServicesApplication.class, args);
    }

}
