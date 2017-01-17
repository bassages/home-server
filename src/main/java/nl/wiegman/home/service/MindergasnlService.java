package nl.wiegman.home.service;

import nl.wiegman.home.model.MeterstandOpDag;
import nl.wiegman.home.model.MindergasnlSettings;
import nl.wiegman.home.repository.MindergasnlSettingsRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class MindergasnlService {

    private static final Logger LOG = LoggerFactory.getLogger(MindergasnlService.class);

    private static final String METERSTAND_UPLOAD_ENDPOINT = "http://www.mindergas.nl/api/gas_meter_readings";

    @Autowired
    MindergasnlSettingsRepository mindergasnlSettingsRepository;

    @Autowired
    MeterstandService meterstandService;

    public List<MindergasnlSettings> getAllSettings() {
        return mindergasnlSettingsRepository.findAll();
    }

    public MindergasnlSettings save(MindergasnlSettings mindergasnlSettings) {
        return mindergasnlSettingsRepository.save(mindergasnlSettings);
    }

    @Scheduled(cron = "0 0 3 * * *") // 3 o'clock in the morning
    public void uploadMeterstand() {

        List<MindergasnlSettings> settings = getAllSettings();

        if (!settings.isEmpty() && getAllSettings().get(0).isAutomatischUploaden()) {

            Date vandaag = new Date();
            Date gisteren = DateUtils.addDays(vandaag, -1);

            List<MeterstandOpDag> meterstandVanGisteren = meterstandService.perDag(gisteren.getTime(), gisteren.getTime());

            if (CollectionUtils.isEmpty(meterstandVanGisteren)) {
                LOG.warn("Failed to upload to mindergas.nl because no meterstand could be found for yesterday");
            } else {
                BigDecimal gasStand = meterstandVanGisteren.get(0).getMeterstand().getGas();

                try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()){
                    HttpPost request = new HttpPost(METERSTAND_UPLOAD_ENDPOINT);

                    String message = String.format("{ \"date\": \"%s\", \"reading\": %s }", new SimpleDateFormat("yyyy-MM-dd").format(gisteren), gasStand.toString());
                    LOG.info("Upload to mindergas.nl: " + message);

                    StringEntity params = new StringEntity(message);

                    request.addHeader("content-type", ContentType.APPLICATION_JSON.getMimeType());
                    request.addHeader("AUTH-TOKEN", getAllSettings().get(0).getAuthenticatietoken());
                    request.setEntity(params);

                    CloseableHttpResponse response = httpClient.execute(request);

                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != 201) {
                        LOG.error("Failed to upload to mindergas.nl. HTTP status code: " + statusCode);
                    }
                } catch (Exception ex) {
                    LOG.error("Failed to upload to mindergas.nl", ex);
                }
            }
        }
    }
}
