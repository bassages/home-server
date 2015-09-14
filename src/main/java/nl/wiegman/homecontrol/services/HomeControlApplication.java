package nl.wiegman.homecontrol.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.text.SimpleDateFormat;

@SpringBootApplication
public class HomeControlApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(HomeControlApplication.class, args);
    }
}
