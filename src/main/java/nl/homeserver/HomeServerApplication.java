package nl.homeserver;

import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.util.concurrent.Executor;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@SpringBootApplication
@EnableAsync
@EnableCaching
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

    @Bean
    @Scope(value = SCOPE_PROTOTYPE)
    public HttpClientBuilder getHttpClientBuilder() {
        return HttpClientBuilder.create();
    }
}
