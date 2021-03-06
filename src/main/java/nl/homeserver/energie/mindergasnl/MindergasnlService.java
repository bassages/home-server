package nl.homeserver.energie.mindergasnl;

import static java.time.format.DateTimeFormatter.ofPattern;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.inject.Provider;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.homeserver.DatePeriod;
import nl.homeserver.energie.meterstand.MeterstandOpDag;
import nl.homeserver.energie.meterstand.MeterstandService;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class MindergasnlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MindergasnlService.class);

    private static final String METER_READING_UPLOAD_URL = "http://www.mindergas.nl/api/gas_meter_readings";

    static final String HEADER_NAME_CONTENT_TYPE = "content-type";
    static final String HEADER_NAME_AUTH_TOKEN = "AUTH-TOKEN";

    private final MindergasnlSettingsRepository mindergasnlSettingsRepository;
    private final MeterstandService meterstandService;
    private final Provider<HttpClientBuilder> httpClientBuilder;
    private final Clock clock;

    public Optional<MindergasnlSettings> findOne() {
        return mindergasnlSettingsRepository.findOneByIdIsNotNull();
    }

    public MindergasnlSettings save(final MindergasnlSettings mindergasnlSettings) {
        final Optional<MindergasnlSettings> optionalExistingMindergasnlSettings = findOne();

        if (optionalExistingMindergasnlSettings.isPresent()) {
            final MindergasnlSettings existingMindergasnlSettings = optionalExistingMindergasnlSettings.get();
            existingMindergasnlSettings.setAutomatischUploaden(mindergasnlSettings.isAutomatischUploaden());
            existingMindergasnlSettings.setAuthenticatietoken(mindergasnlSettings.getAuthenticatietoken());
            return mindergasnlSettingsRepository.save(existingMindergasnlSettings);
        } else {
            return mindergasnlSettingsRepository.save(mindergasnlSettings);
        }
    }

    public void uploadMeterstand(final MindergasnlSettings settings) {
        final LocalDate today = LocalDate.now(clock);
        final LocalDate yesterday = today.minusDays(1);

        final DatePeriod period = aPeriodWithToDate(yesterday, today);

        final List<MeterstandOpDag> yesterdaysLastMeterReading = meterstandService.getPerDag(period);

        if (isEmpty(yesterdaysLastMeterReading)) {
            LOGGER.warn("Failed to upload to mindergas.nl because no meter reading could be found for date {}", yesterday);
            return;
        }

        final BigDecimal gasReading = yesterdaysLastMeterReading.get(0).getMeterstand().getGas();

        try (final CloseableHttpClient httpClient = httpClientBuilder.get().build()){
            final HttpPost request = createRequest(yesterday, gasReading, settings.getAuthenticatietoken());
            final CloseableHttpResponse response = httpClient.execute(request);
            logErrorWhenNoSuccess(response);
        } catch (final Exception ex) {
            LOGGER.error("Failed to upload to mindergas.nl", ex);
        }
    }

    private HttpPost createRequest(final LocalDate day,
                                   final BigDecimal gasReading,
                                   final String authenticationToken) throws UnsupportedEncodingException {
        final HttpPost request = new HttpPost(METER_READING_UPLOAD_URL);

        final String message = """
            { "date": "%s", "reading": %s }
            """.formatted(day.format(ofPattern("yyyy-MM-dd")), gasReading.toString());

        LOGGER.info("Upload to mindergas.nl: {}", message);

        request.addHeader(HEADER_NAME_CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        request.addHeader(HEADER_NAME_AUTH_TOKEN, authenticationToken);

        final StringEntity params = new StringEntity(message);
        request.setEntity(params);
        return request;
    }

    private void logErrorWhenNoSuccess(final CloseableHttpResponse response) {
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 201) {
            LOGGER.error("Failed to upload to mindergas.nl. HTTP status code: {}", statusCode);
        }
    }
}
