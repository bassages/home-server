package nl.homeserver.energy.mindergasnl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    locations = "/integrationtests.properties",
    properties = "home-server.mindergasnl.scheduleduploader.cron=" + ScheduledUploaderIntegrationTest.EVERY_SECOND
)
class ScheduledUploaderIntegrationTest {

    static final String EVERY_SECOND = "0/2 * * * * ?";

    @MockitoSpyBean
    ScheduledUploader scheduledUploader;

    @Test
    void whenTheApplicationIsRunningThenUploadingToMindergasnlIsScheduled() {
        await()
            .atMost(Duration.ofMillis(4100))
            .untilAsserted(() -> verify(scheduledUploader, times(2)).uploadMeterstandWhenEnabled());
    }
}
