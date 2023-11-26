package nl.homeserver.energy.mindergasnl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.energy.meterreading.MeterstandService;
import nl.homeserver.energy.mindergasnl.MinderGasnlApi.MinderGasnlMeterReading;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
class MindergasnlService {

    @Value("${home-server.mindergas.api.url}")
    String mindergasNlApiUrl;

    private final MindergasnlSettingsRepository mindergasnlSettingsRepository;
    private final MeterstandService meterstandService;
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
        final WebClient client = WebClient.builder().baseUrl(mindergasNlApiUrl).build();
        final HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(client)).build();
        final MinderGasnlApi api = factory.createClient(MinderGasnlApi.class);

        HttpStatusCode httpStatusCode;
        try {
            httpStatusCode = api
                    .meterReading(settings.getAuthenticatietoken(), new MinderGasnlMeterReading(day, gasReading))
                    .getStatusCode();
        } catch (final WebClientResponseException e) {
            httpStatusCode = e.getStatusCode();
        }
        if (httpStatusCode.value() != HttpStatus.CREATED.value()) {
            log.error("Failed to upload to mindergas.nl. HTTP status code: {}", httpStatusCode.value());
        }
    }
}
