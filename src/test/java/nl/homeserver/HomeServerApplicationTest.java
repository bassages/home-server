package nl.homeserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.util.concurrent.Executor;

import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

public class HomeServerApplicationTest {

    @Test
    public void whenGetClockThenSystemDefautClockReturned() {
        HomeServerApplication homeServerApplication = new HomeServerApplication();

        Clock clock = homeServerApplication.getClock();

        assertThat(clock).isEqualTo(Clock.systemDefaultZone());
    }

    @Test
    public void whenGetHttpClientBuilderThenReturned() {
        HomeServerApplication homeServerApplication = new HomeServerApplication();

        HttpClientBuilder httpClientBuilder = homeServerApplication.getHttpClientBuilder();

        assertThat(httpClientBuilder).isNotNull();
    }

    @Test
    public void whenGeTaskExcecutorThenReturned() {
        HomeServerApplication homeServerApplication = new HomeServerApplication();

        Executor taskExecutor = homeServerApplication.getTaskExecutor();

        assertThat(taskExecutor).isExactlyInstanceOf(SimpleAsyncTaskExecutor.class);
    }
}