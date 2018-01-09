package nl.homeserver;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import java.time.Clock;
import java.util.concurrent.Executor;

import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class HomeServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeServerApplication.class, args);
    }

    @Bean
    public Executor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @Scope(value = SCOPE_PROTOTYPE)
    public HttpClientBuilder httpClientBuilder() {
        return HttpClientBuilder.create();
    }
}
