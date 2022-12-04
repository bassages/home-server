package nl.homeserver.energy.mindergasnl;

import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homeserver.CaptureLogging;
import nl.homeserver.energy.meterreading.Meterstand;
import nl.homeserver.energy.meterreading.MeterstandService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static ch.qos.logback.classic.Level.ERROR;
import static ch.qos.logback.classic.Level.WARN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Month.JANUARY;
import static nl.homeserver.energy.meterreading.MeterstandBuilder.aMeterstand;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MindergasnlServiceTest {

    @InjectMocks
    MindergasnlService mindergasnlService;

    @Mock
    MindergasnlSettingsRepository mindergasnlSettingsRepository;
    @Mock
    MeterstandService meterstandService;
    @Mock
    Clock clock;

    @Test
    void givenNoSettingsExistsWhenSaveThenSavedInToRepository() {
        // given
        when(mindergasnlSettingsRepository.findOneByIdIsNotNull()).thenReturn(Optional.empty());
        final MindergasnlSettings mindergasnlSettings = mock(MindergasnlSettings.class);

        when(mindergasnlSettingsRepository.save(mindergasnlSettings)).thenReturn(mindergasnlSettings);

        // when
        final MindergasnlSettings savedMindergasnlSettings = mindergasnlService.save(mindergasnlSettings);

        // then
        assertThat(savedMindergasnlSettings).isEqualTo(mindergasnlSettings);
    }

    @Test
    void givenSettingsExistsWhenSaveThenDelegatedToRepository() {
        // given
        final MindergasnlSettings existingMindergasnlSettings = new MindergasnlSettings();
        when(mindergasnlSettingsRepository.findOneByIdIsNotNull()).thenReturn(Optional.of(existingMindergasnlSettings));

        final MindergasnlSettings updatedMindergasnlSettings = new MindergasnlSettings();
        updatedMindergasnlSettings.setAutomatischUploaden(false);
        updatedMindergasnlSettings.setAuthenticatietoken("newToken");

        when(mindergasnlSettingsRepository.save(any(MindergasnlSettings.class))).then(returnsFirstArg());

        // when
        final MindergasnlSettings savedMindergasnlSettings = mindergasnlService.save(updatedMindergasnlSettings);

        // then
        assertThat(savedMindergasnlSettings).isEqualTo(existingMindergasnlSettings);
        assertThat(existingMindergasnlSettings.getAuthenticatietoken()).isSameAs(
                updatedMindergasnlSettings.getAuthenticatietoken());
        assertThat(existingMindergasnlSettings.isAutomatischUploaden()).isSameAs(
                updatedMindergasnlSettings.isAutomatischUploaden());
    }

    @Test
    void givenMindergasnlSettingExistWhenFindOneThenOptionalContainingMindergasnlSettingsReturned() {
        // given
        final MindergasnlSettings mindergasnlSettings = mock(MindergasnlSettings.class);
        when(mindergasnlSettingsRepository.findOneByIdIsNotNull()).thenReturn(Optional.of(mindergasnlSettings));

        // when
        final Optional<MindergasnlSettings> actual = mindergasnlService.findSettings();

        // then
        assertThat(actual).contains(mindergasnlSettings);
    }

    @Test
    void givenMindergasnlSettingNotExistWhenFindOneSettingsThenEmptyOptionalReturned() {
        // given
        when(mindergasnlSettingsRepository.findOneByIdIsNotNull()).thenReturn(Optional.empty());

        // when
        final Optional<MindergasnlSettings> actualMinderGasnlSettings = mindergasnlService.findSettings();

        // then
        assertThat(actualMinderGasnlSettings).isEmpty();
    }

    @Test
    void givenSomeMinderGasnlSettingsWhenUploadMeterstandThenUploaded() throws Exception {
        // given
        final LocalDateTime currentDateTime = LocalDate.of(2018, JANUARY, 2).atTime(17, 9);
        timeTravelTo(clock, currentDateTime);
        final LocalDate yesterday = currentDateTime.minusDays(1).toLocalDate();

        final MindergasnlSettings mindergasnlSettings = new MindergasnlSettings();
        mindergasnlSettings.setAutomatischUploaden(true);
        mindergasnlSettings.setAuthenticatietoken("LetMeIn");

        final Meterstand yesterDaysMostRecentMeterstand = aMeterstand()
                .withGas(new BigDecimal("12412.812"))
                .build();
        when(meterstandService.getMeesteRecenteMeterstandOpDag(yesterday))
                .thenReturn(Optional.of(yesterDaysMostRecentMeterstand));

        try (final MockWebServer mockBackEnd = new MockWebServer()) {
            mockBackEnd.start();
            mockBackEnd.enqueue(new MockResponse().setResponseCode(200));
            mindergasnlService.mindergasNlApiUrl = "http://" + mockBackEnd.getHostName() + ":" + mockBackEnd.getPort();

            // when
            mindergasnlService.uploadMostRecentMeterstand(mindergasnlSettings);

            // then
            final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
            assertThat(recordedRequest.getMethod()).isEqualTo("POST");
            assertThat(recordedRequest.getPath()).isEqualTo("/meter_readings");
            assertThat(recordedRequest.getHeader(MinderGasnlApi.HEADER_NAME_AUTH_TOKEN))
                    .isEqualTo(mindergasnlSettings.getAuthenticatietoken());
            JSONAssert.assertEquals(
                    """
                    { "date": "2018-01-01", "reading": 12412.812 }
                    """, recordedRequest.getBody().readString(UTF_8), JSONCompareMode.STRICT);
        }
    }

    @CaptureLogging(MindergasnlService.class)
    @Test
    void givenYesterdaysMeterstandIsUnknownWhenUploadMeterstandWhenEnabledThenNotUploaded(
            final ArgumentCaptor<LoggingEvent> loggerEventCaptor) {

        // given
        final LocalDateTime currentDateTime = LocalDate.of(2018, JANUARY, 2).atTime(17, 9);
        timeTravelTo(clock, currentDateTime);
        final LocalDate yesterday = currentDateTime.minusDays(1).toLocalDate();

        final MindergasnlSettings mindergasnlSettings = new MindergasnlSettings();
        mindergasnlSettings.setAutomatischUploaden(true);

        when(meterstandService.getMeesteRecenteMeterstandOpDag(yesterday))
                .thenReturn(Optional.empty());

        // when
        mindergasnlService.uploadMostRecentMeterstand(mindergasnlSettings);

        // then
        verify(meterstandService).getMeesteRecenteMeterstandOpDag(yesterday);

        final LoggingEvent loggingEvent = loggerEventCaptor.getValue();
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo(
                "Failed to upload to mindergas.nl because no meter reading could be found for date 2018-01-01");
        assertThat(loggingEvent.getLevel()).isEqualTo(WARN);
    }

    @CaptureLogging(MindergasnlService.class)
    @Test
    void givenMinderGasNlRespondsWithOtherThanStatus20xWhenUploadMeterstandThenErrorLogged(
            final ArgumentCaptor<LoggingEvent> loggerEventCaptor) throws Exception {

        // given
        final LocalDateTime currentDateTime = LocalDate.of(2018, JANUARY, 2).atTime(17, 9);
        timeTravelTo(clock, currentDateTime);
        final LocalDate yesterday = currentDateTime.minusDays(1).toLocalDate();

        final MindergasnlSettings mindergasnlSettings = new MindergasnlSettings();
        mindergasnlSettings.setAutomatischUploaden(true);
        mindergasnlSettings.setAuthenticatietoken("LetMeIn");

        final Meterstand yesterDaysMostRecentMeterstand = aMeterstand()
                .withGas(new BigDecimal("12412.812"))
                .build();
        when(meterstandService.getMeesteRecenteMeterstandOpDag(yesterday))
                .thenReturn(Optional.of(yesterDaysMostRecentMeterstand));

        try (final MockWebServer mockBackEnd = new MockWebServer()) {
            mockBackEnd.start();
            mockBackEnd.enqueue(new MockResponse().setResponseCode(500));
            mindergasnlService.mindergasNlApiUrl = "http://" + mockBackEnd.getHostName() + ":" + mockBackEnd.getPort();

            // when
            mindergasnlService.uploadMostRecentMeterstand(mindergasnlSettings);

            // then
            final LoggingEvent loggingEvent = loggerEventCaptor.getValue();
            assertThat(loggingEvent.getFormattedMessage()).isEqualTo("Failed to upload to mindergas.nl. HTTP status code: 500");
            assertThat(loggingEvent.getLevel()).isEqualTo(ERROR);
        }
    }

    @Test
    void whenFindOneThenRetievedFromRepository() {
        // given
        final Optional<MindergasnlSettings> mindergasnlSettings = Optional.of(mock(MindergasnlSettings.class));
        when(mindergasnlSettingsRepository.findOneByIdIsNotNull()).thenReturn(mindergasnlSettings);

        // when
        final Optional<MindergasnlSettings> actualResult = mindergasnlService.findSettings();

        // then
        assertThat(actualResult).isSameAs(mindergasnlSettings);
    }
}
