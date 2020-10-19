package nl.homeserver.energie.mindergasnl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(
    locations = "/integrationtests.properties",
    properties = "mindergasnl.scheduleduploader.cron=" + ScheduledUploaderIntegrationTest.EVERY_SECOND
)
public class ScheduledUploaderIntegrationTest {

    static final String EVERY_SECOND = "0/1 * * * * ?";

    @SpyBean
    private ScheduledUploader scheduledUploader;

    @Test
    public void whenTheApplicationIsRunningThenUploadingToMindergasnlIsScheduled() {
        await()
            .atMost(Duration.ofMillis(2500))
            .untilAsserted(() -> verify(scheduledUploader, times(2)).uploadMeterstandWhenEnabled());
    }
}
