package nl.homeserver;

import java.time.Clock;
import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class HomeServerApplication {

    public static void main(final String[] args) {
        SpringApplication.run(HomeServerApplication.class, args);
    }

    @Bean
    public Executor getTaskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public Clock getClock() {
        return Clock.systemDefaultZone();
    }
}
