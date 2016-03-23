package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.MindergasnlSettings;
import nl.wiegman.homecontrol.services.repository.MindergasnlSettingsRepository;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class MindergasnlService {

    private final Logger logger = LoggerFactory.getLogger(MindergasnlService.class);

    private static final String METERSTAND_UPLOAD_ENDPOINT = "http://www.mindergas.nl/api/gas_meter_readings";

    @Inject
    MindergasnlSettingsRepository mindergasnlSettingsRepository;
    @Inject
    MeterstandService meterstandService;

    public List<MindergasnlSettings> getAllSettings() {
        return mindergasnlSettingsRepository.findAll();
    }

    public MindergasnlSettings save(MindergasnlSettings mindergasnlSettings) {
        return mindergasnlSettingsRepository.save(mindergasnlSettings);
    }

//    @Scheduled(cron = "*/30 * * * * *") // Elke 30 seconden
    @Scheduled(cron = "0 0 3 * * *") // 3 uur 's nachts
    public void uploadMeterstand() {

        List<MindergasnlSettings> settings = getAllSettings();

        if (!settings.isEmpty() && getAllSettings().get(0).isAutomatischUploaden()) {

            Date vandaag = new Date();
            Date gisteren = DateUtils.addDays(vandaag, -1);

            BigDecimal gasStand = meterstandService.perDag(gisteren.getTime(), gisteren.getTime()).get(0).getMeterstand().getGas();

            try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()){
                HttpPost request = new HttpPost(METERSTAND_UPLOAD_ENDPOINT);

                String message = String.format("{ \"date\": \"%s\", \"reading\": %s }", new SimpleDateFormat("yyyy-MM-dd").format(gisteren), gasStand.toString());
                logger.info("Upload to mindergas.nl: " + message);

                StringEntity params = new StringEntity(message);

                request.addHeader("content-type", ContentType.APPLICATION_JSON.getMimeType());
                request.addHeader("AUTH-TOKEN", getAllSettings().get(0).getAuthenticatietoken());
                request.setEntity(params);

                CloseableHttpResponse response = httpClient.execute(request);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 201) {
                    logger.error("Failed to upload to mindergas.nl. HTTP status code: " + statusCode);
                }
            } catch (Exception ex) {
                logger.error("Failed to upload to mindergas.nl", ex);
            }
        }
    }
}
