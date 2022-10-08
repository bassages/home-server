package nl.homeserver.energie.mindergasnl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

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

    @SpyBean
    ScheduledUploader scheduledUploader;

    @Test
    void whenTheApplicationIsRunningThenUploadingToMindergasnlIsScheduled() {
        await()
            .atMost(Duration.ofMillis(4100))
            .untilAsserted(() -> verify(scheduledUploader, times(2)).uploadMeterstandWhenEnabled());
    }
}
