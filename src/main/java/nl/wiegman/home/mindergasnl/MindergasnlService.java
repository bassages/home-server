package nl.wiegman.home.mindergasnl;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import nl.wiegman.home.DateTimePeriod;
import nl.wiegman.home.energie.MeterstandOpDag;
import nl.wiegman.home.energie.MeterstandService;

@Service
public class MindergasnlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MindergasnlService.class);

    private static final String METERSTAND_UPLOAD_ENDPOINT = "http://www.mindergas.nl/api/gas_meter_readings";
    private static final String THREE_AM = "0 0 3 * * *";

    private final MindergasnlSettingsRepository mindergasnlSettingsRepository;
    private final MeterstandService meterstandService;
    private final Clock clock;

    @Autowired
    public MindergasnlService(MindergasnlSettingsRepository mindergasnlSettingsRepository, MeterstandService meterstandService, Clock clock) {
        this.mindergasnlSettingsRepository = mindergasnlSettingsRepository;
        this.meterstandService = meterstandService;
        this.clock = clock;
    }

    public List<MindergasnlSettings> getAllSettings() {
        return mindergasnlSettingsRepository.findAll();
    }

    public MindergasnlSettings save(MindergasnlSettings mindergasnlSettings) {
        return mindergasnlSettingsRepository.save(mindergasnlSettings);
    }

    @Scheduled(cron = THREE_AM)
    public void uploadMeterstand() {

        List<MindergasnlSettings> settings = getAllSettings();

        if (!settings.isEmpty() && getAllSettings().get(0).isAutomatischUploaden()) {

            LocalDateTime todayAtStartOfDay = LocalDate.now(clock).atStartOfDay();
            LocalDateTime yesterdayAtStartOfDay = todayAtStartOfDay.minusDays(1);

            DateTimePeriod period = DateTimePeriod.aPeriodWithToDateTime(yesterdayAtStartOfDay, todayAtStartOfDay);

            List<MeterstandOpDag> yesterdaysLastMeterstand = meterstandService.perDag(period);

            if (isEmpty(yesterdaysLastMeterstand)) {
                LOGGER.warn("Failed to upload to mindergas.nl because no meterstand could be found for date {}", yesterdayAtStartOfDay);
            } else {
                BigDecimal gasStand = yesterdaysLastMeterstand.get(0).getMeterstand().getGas();

                try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()){
                    HttpPost request = new HttpPost(METERSTAND_UPLOAD_ENDPOINT);

                    String message = String.format("{ \"date\": \"%s\", \"reading\": %s }", yesterdayAtStartOfDay.format(ofPattern("yyyy-MM-dd")), gasStand.toString());
                    LOGGER.info("Upload to mindergas.nl: " + message);

                    request.addHeader("content-type", ContentType.APPLICATION_JSON.getMimeType());
                    request.addHeader("AUTH-TOKEN", getAllSettings().get(0).getAuthenticatietoken());

                    StringEntity params = new StringEntity(message);
                    request.setEntity(params);

                    CloseableHttpResponse response = httpClient.execute(request);

                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != 201) {
                        LOGGER.error("Failed to upload to mindergas.nl. HTTP status code: " + statusCode);
                    }
                } catch (Exception ex) {
                    LOGGER.error("Failed to upload to mindergas.nl", ex);
                }
            }
        }
    }
}
