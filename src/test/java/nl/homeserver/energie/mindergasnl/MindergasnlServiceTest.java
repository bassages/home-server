package nl.homeserver.energie.mindergasnl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homeserver.CaptureLogging;
import nl.homeserver.energie.meterstand.Meterstand;
import nl.homeserver.energie.meterstand.MeterstandService;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.inject.Provider;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.Month.JANUARY;
import static nl.homeserver.energie.meterstand.MeterstandBuilder.aMeterstand;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
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
    Provider<HttpClientBuilder> httpClientBuilderProvider;
    @Mock
    HttpClientBuilder httpClientBuilder;
    @Mock
    Clock clock;

    @Mock
    CloseableHttpClient closeableHttpClient;
    @Mock
    CloseableHttpResponse closeableHttpResponse;
    @Mock
    StatusLine statusLine;

    @Captor
    ArgumentCaptor<HttpUriRequest> httpUriRequestCaptor;

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

        when(httpClientBuilderProvider.get()).thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(closeableHttpClient);
        when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);

        // when
        mindergasnlService.uploadMostRecentMeterstand(mindergasnlSettings);

        // then
        verify(closeableHttpClient).execute(httpUriRequestCaptor.capture());

        assertThat(httpUriRequestCaptor.getValue()).isExactlyInstanceOf(HttpPost.class);
        final HttpPost httpPost = (HttpPost) httpUriRequestCaptor.getValue();

        assertThat(httpPost.getURI()).hasScheme("http");
        assertThat(httpPost.getURI()).hasHost("www.mindergas.nl");

        assertThat(httpPost.getHeaders(MindergasnlService.HEADER_NAME_AUTH_TOKEN))
                .extracting(Header::getValue)
                .containsExactly(mindergasnlSettings.getAuthenticatietoken());

        assertThat(httpPost.getHeaders(MindergasnlService.HEADER_NAME_CONTENT_TYPE))
                .extracting(Header::getValue)
                .containsExactly(APPLICATION_JSON.getMimeType());

        assertThat(httpPost.getEntity()).isNotNull();

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            httpPost.getEntity().writeTo(baos);
            assertThat(baos).hasToString("""
                    { "date": "2018-01-01", "reading": 12412.812 }
                    """);
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
        verifyNoMoreInteractions(httpClientBuilder);

        final LoggingEvent loggingEvent = loggerEventCaptor.getValue();
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo(
                "Failed to upload to mindergas.nl because no meter reading could be found for date 2018-01-01");
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.WARN);
    }

    @CaptureLogging(MindergasnlService.class)
    @Test
    void givenMinderGasNlRespondsWithOtherThanStatus201WhenUploadMeterstandThenErrorLogged(
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

        when(httpClientBuilderProvider.get()).thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(closeableHttpClient);
        when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);

        // when
        mindergasnlService.uploadMostRecentMeterstand(mindergasnlSettings);

        // then
        verify(closeableHttpClient).execute(httpUriRequestCaptor.capture());

        final LoggingEvent loggingEvent = loggerEventCaptor.getValue();
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo("Failed to upload to mindergas.nl. HTTP status code: 403");
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.ERROR);
    }

    @CaptureLogging(MindergasnlService.class)
    @Test
    void givenHttpClientBuilderProviderThrowsExceptionWhenUploadMeterstandThenErrorLogged(
            final ArgumentCaptor<LoggingEvent> loggerEventCaptor) {

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

        final RuntimeException runtimeException = new RuntimeException("FUBAR");
        when(httpClientBuilderProvider.get()).thenThrow(runtimeException);

        // when
        mindergasnlService.uploadMostRecentMeterstand(mindergasnlSettings);

        // then
        final LoggingEvent loggingEvent = loggerEventCaptor.getValue();
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo("Failed to upload to mindergas.nl");
        assertThat(loggingEvent.getThrowableProxy().getClassName()).isEqualTo(runtimeException.getClass().getName());
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
