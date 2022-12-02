package nl.homeserver.energy.mindergasnl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.energy.meterreading.MeterstandService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MindergasnlService {

    @Value("${home-server.mindergas.api.url}")
    protected String mindergasNlApiUrl;

    static final String HEADER_NAME_CONTENT_TYPE = "content-type";
    static final String HEADER_NAME_AUTH_TOKEN = "AUTH-TOKEN";

    private final MindergasnlSettingsRepository mindergasnlSettingsRepository;
    private final MeterstandService meterstandService;
    private final HttpClientBuilder httpClientBuilder;
    private final Clock clock;

    public Optional<MindergasnlSettings> findSettings() {
        return mindergasnlSettingsRepository.findOneByIdIsNotNull();
    }

    public MindergasnlSettings save(final MindergasnlSettings mindergasnlSettings) {
        final Optional<MindergasnlSettings> optionalExistingMindergasnlSettings = findSettings();

        if (optionalExistingMindergasnlSettings.isPresent()) {
            final MindergasnlSettings existingMindergasnlSettings = optionalExistingMindergasnlSettings.get();
            existingMindergasnlSettings.setAutomatischUploaden(mindergasnlSettings.isAutomatischUploaden());
            existingMindergasnlSettings.setAuthenticatietoken(mindergasnlSettings.getAuthenticatietoken());
            return mindergasnlSettingsRepository.save(existingMindergasnlSettings);
        } else {
            return mindergasnlSettingsRepository.save(mindergasnlSettings);
        }
    }

    public void uploadMostRecentMeterstand(final MindergasnlSettings settings) {
        final LocalDate today = LocalDate.now(clock);
        final LocalDate yesterday = today.minusDays(1);

        meterstandService
            .getMeesteRecenteMeterstandOpDag(yesterday)
            .ifPresentOrElse(
                yesterdaysMostRecentMeterstand -> upload(settings, yesterday, yesterdaysMostRecentMeterstand.getGas()),
                () -> log.warn("Failed to upload to mindergas.nl because no meter reading could be found for date {}", yesterday));
    }

    private void upload(final MindergasnlSettings settings, final LocalDate day, final BigDecimal gasReading) {
        try (final CloseableHttpClient httpClient = httpClientBuilder.build()){
            final HttpPost request = createRequest(day, gasReading, settings.getAuthenticatietoken());
            final CloseableHttpResponse response = httpClient.execute(request);
            logErrorWhenNoSuccess(response);
        } catch (final Exception ex) {
            log.error("Failed to upload to mindergas.nl", ex);
        }
    }

    private HttpPost createRequest(final LocalDate day,
                                   final BigDecimal gasReading,
                                   final String authenticationToken) throws UnsupportedEncodingException {
        final HttpPost request = new HttpPost(mindergasNlApiUrl);

        final String message = """
            { "date": "%s", "reading": %s }
            """.formatted(day.format(ofPattern("yyyy-MM-dd")), gasReading.toString());

        log.info("Upload to mindergas.nl: {}", message);

        request.addHeader(HEADER_NAME_CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        request.addHeader(HEADER_NAME_AUTH_TOKEN, authenticationToken);

        final StringEntity params = new StringEntity(message);
        request.setEntity(params);
        return request;
    }

    private void logErrorWhenNoSuccess(final CloseableHttpResponse response) {
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 201) {
            log.error("Failed to upload to mindergas.nl. HTTP status code: {}", statusCode);
        }
    }
}
