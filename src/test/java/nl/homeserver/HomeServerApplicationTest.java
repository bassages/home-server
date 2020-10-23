package nl.homeserver;

import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.time.Clock;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

class HomeServerApplicationTest {

    @Test
    void whenGetClockThenSystemDefautClockReturned() {
        final HomeServerApplication homeServerApplication = new HomeServerApplication();

        final Clock clock = homeServerApplication.getClock();

        assertThat(clock).isEqualTo(Clock.systemDefaultZone());
    }

    @Test
    void whenGetHttpClientBuilderThenReturned() {
        final HomeServerApplication homeServerApplication = new HomeServerApplication();

        final HttpClientBuilder httpClientBuilder = homeServerApplication.getHttpClientBuilder();

        assertThat(httpClientBuilder).isNotNull();
    }

    @Test
    void whenGetTaskExcecutorThenReturned() {
        final HomeServerApplication homeServerApplication = new HomeServerApplication();

        final Executor taskExecutor = homeServerApplication.getTaskExecutor();

        assertThat(taskExecutor).isExactlyInstanceOf(SimpleAsyncTaskExecutor.class);
    }
}
