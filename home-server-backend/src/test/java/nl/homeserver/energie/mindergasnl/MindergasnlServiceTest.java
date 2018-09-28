package nl.homeserver.energie.mindergasnl;

import static java.util.Collections.emptyList;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.energie.meterstand.MeterstandBuilder.aMeterstand;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.inject.Provider;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homeserver.DatePeriod;
import nl.homeserver.LoggingRule;
import nl.homeserver.energie.meterstand.MeterstandOpDag;
import nl.homeserver.energie.meterstand.MeterstandService;

@RunWith(MockitoJUnitRunner.class)
public class MindergasnlServiceTest {

    @InjectMocks
    private MindergasnlService mindergasnlService;

    @Mock
    private MindergasnlSettingsRepository mindergasnlSettingsRepository;
    @Mock
    private MeterstandService meterstandService;
    @Mock
    private Provider<HttpClientBuilder> httpClientBuilderProvider;
    @Mock
    private HttpClientBuilder httpClientBuilder;
    @Mock
    private Clock clock;

    @Mock
    private CloseableHttpClient closeableHttpClient;
    @Mock
    private CloseableHttpResponse closeableHttpResponse;
    @Mock
    private StatusLine statusLine;

    @Rule
    public LoggingRule loggingRule = new LoggingRule(MindergasnlService.class);

    @Captor
    private ArgumentCaptor<HttpUriRequest> httpUriRequestCaptor;

    @Test
    public void givenNoSettingsExistsWhenSaveSettingsThenDelegatedToRepository() {
        when(mindergasnlSettingsRepository.findOneByIdIsNotNull()).thenReturn(Optional.empty());

        final MindergasnlSettings mindergasnlSettings = mock(MindergasnlSettings.class);

        when(mindergasnlSettingsRepository.save(mindergasnlSettings)).thenReturn(mindergasnlSettings);

        assertThat(mindergasnlService.save(mindergasnlSettings)).isEqualTo(mindergasnlSettings);
    }

    @Test
    public void givenSettingsExistsWhenSaveSettingsThenDelegatedToRepository() {
        final MindergasnlSettings existingMindergasnlSettings = new MindergasnlSettings();
        when(mindergasnlSettingsRepository.findOneByIdIsNotNull()).thenReturn(Optional.of(existingMindergasnlSettings));

        final MindergasnlSettings updatedMindergasnlSettings = new MindergasnlSettings();
        updatedMindergasnlSettings .setAutomatischUploaden(false);
        updatedMindergasnlSettings.setAuthenticatietoken("newToken");

        when(mindergasnlSettingsRepository.save(any(MindergasnlSettings.class))).then(returnsFirstArg());

        assertThat(mindergasnlService.save(updatedMindergasnlSettings)).isEqualTo(existingMindergasnlSettings);

        assertThat(existingMindergasnlSettings.getAuthenticatietoken()).isSameAs(updatedMindergasnlSettings.getAuthenticatietoken());
        assertThat(existingMindergasnlSettings.isAutomatischUploaden()).isSameAs(updatedMindergasnlSettings.isAutomatischUploaden());
    }

    @Test
    public void givenMindergasnlSettingExistWhenFindOneSettingsThenOptionalContainingMindergasnlSettingsReturned() {
        final MindergasnlSettings mindergasnlSettings = mock(MindergasnlSettings.class);

        when(mindergasnlSettingsRepository.findOneByIdIsNotNull()).thenReturn(Optional.of(mindergasnlSettings));

        assertThat(mindergasnlService.findOne()).contains(mindergasnlSettings);
    }

    @Test
    public void givenMindergasnlSettingNotExistWhenFindOneSettingsThenEmptyOptionalReturned() {
        final MindergasnlSettings mindergasnlSettings = mock(MindergasnlSettings.class);

        when(mindergasnlSettingsRepository.findOneByIdIsNotNull()).thenReturn(Optional.of(mindergasnlSettings));

        assertThat(mindergasnlService.findOne()).contains(mindergasnlSettings);
    }

    @Test
    public void givenNoMindergasnlSettingsExistWhenUploadMeterstandWhenEnabledThenNotUploaded() {
        timeTravelTo(clock, LocalDate.of(2018, 1, 2).atTime(17, 9));

        when(mindergasnlSettingsRepository.findOneByIdIsNotNull()).thenReturn(Optional.empty());

        mindergasnlService.uploadMeterstandWhenEnabled();

        verifyZeroInteractions(meterstandService, httpClientBuilder);
    }

    @Test
    public void givenAutomaticUploadIsDisabledWhenUploadMeterstandWhenEnabledThenNotUploaded() {
        timeTravelTo(clock, LocalDate.of(2018, 1, 2).atTime(17, 9));

        final MindergasnlSettings mindergasnlSettings = new MindergasnlSettings();
        mindergasnlSettings.setAutomatischUploaden(false);
        when(mindergasnlSettingsRepository.findOneByIdIsNotNull()).thenReturn(Optional.of(mindergasnlSettings));

        mindergasnlService.uploadMeterstandWhenEnabled();

        verifyZeroInteractions(meterstandService, httpClientBuilder);
    }

    @Test
    public void givenAutomaticUploadIsEnabledWhenUploadMeterstandWhenEnabledThenUploaded() throws Exception {
        final LocalDateTime currentDateTime = LocalDate.of(2018, 1, 2).atTime(17, 9);
        timeTravelTo(clock, currentDateTime);

        final MindergasnlSettings mindergasnlSettings = new MindergasnlSettings();
        mindergasnlSettings.setAutomatischUploaden(true);
        mindergasnlSettings.setAuthenticatietoken("LetMeIn");
        when(mindergasnlSettingsRepository.findOneByIdIsNotNull()).thenReturn(Optional.of(mindergasnlSettings));

        final BigDecimal yesterdaysGas = new BigDecimal("12412.812");
        final MeterstandOpDag yesterDaysMeterstand = new MeterstandOpDag(currentDateTime.minusDays(1).toLocalDate(), aMeterstand().withGas(yesterdaysGas).build());

        final DatePeriod expectedPeriod = aPeriodWithToDate(currentDateTime.minusDays(1).toLocalDate(), currentDateTime.toLocalDate());
        when(meterstandService.getPerDag(eq(expectedPeriod))).thenReturn(List.of(yesterDaysMeterstand));

        when(httpClientBuilderProvider.get()).thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(closeableHttpClient);
        when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);

        mindergasnlService.uploadMeterstandWhenEnabled();

        verify(closeableHttpClient).execute(httpUriRequestCaptor.capture());

        assertThat(httpUriRequestCaptor.getValue()).isExactlyInstanceOf(HttpPost.class);
        final HttpPost httpPost = (HttpPost)httpUriRequestCaptor.getValue();

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
            assertThat(baos.toString()).isEqualTo("{ \"date\": \"2018-01-01\", \"reading\": 12412.812 }");
        }
    }

    @Test
    public void givenYesterdaysMeterstandIsUnknownWhenUploadMeterstandWhenEnabledThenNotUploaded() throws Exception {
        final LocalDateTime currentDateTime = LocalDate.of(2018, 1, 2).atTime(17, 9);
        timeTravelTo(clock, currentDateTime);

        final MindergasnlSettings mindergasnlSettings = new MindergasnlSettings();
        mindergasnlSettings.setAutomatischUploaden(true);
        when(mindergasnlSettingsRepository.findOneByIdIsNotNull()).thenReturn(Optional.of(mindergasnlSettings));

        final DatePeriod expectedPeriod = aPeriodWithToDate(currentDateTime.minusDays(1).toLocalDate(), currentDateTime.toLocalDate());
        when(meterstandService.getPerDag(eq(expectedPeriod))).thenReturn(emptyList());

        mindergasnlService.uploadMeterstandWhenEnabled();

        verify(meterstandService).getPerDag(eq(expectedPeriod));
        verifyZeroInteractions(httpClientBuilder);
    }

    @Test
    public void givenMinderGasNlRespondsWithOtherThanStatus201WhenUploadMeterstandThenErrorLogged() throws Exception {
        final LocalDateTime currentDateTime = LocalDate.of(2018, 1, 2).atTime(17, 9);
        timeTravelTo(clock, currentDateTime);

        final MindergasnlSettings mindergasnlSettings = new MindergasnlSettings();
        mindergasnlSettings.setAutomatischUploaden(true);
        mindergasnlSettings.setAuthenticatietoken("LetMeIn");
        when(mindergasnlSettingsRepository.findOneByIdIsNotNull()).thenReturn(Optional.of(mindergasnlSettings));

        final BigDecimal yesterdaysGas = new BigDecimal("12412.812");
        final MeterstandOpDag yesterDaysMeterstand = new MeterstandOpDag(currentDateTime.minusDays(1).toLocalDate(), aMeterstand().withGas(yesterdaysGas).build());

        final DatePeriod expectedPeriod = aPeriodWithToDate(currentDateTime.minusDays(1).toLocalDate(), currentDateTime.toLocalDate());
        when(meterstandService.getPerDag(eq(expectedPeriod))).thenReturn(List.of(yesterDaysMeterstand));

        when(httpClientBuilderProvider.get()).thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(closeableHttpClient);
        when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);

        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);

        loggingRule.setLevel(Level.ERROR);

        mindergasnlService.uploadMeterstandWhenEnabled();

        verify(closeableHttpClient).execute(httpUriRequestCaptor.capture());

        final LoggingEvent loggingEvent = loggingRule.getLoggedEventCaptor().getValue();
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo("Failed to upload to mindergas.nl. HTTP status code: 403");
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.ERROR);
    }

    @Test
    public void givenHttpClientBuilderProviderThrowsExceptionWhenUploadMeterstandThenErrorLogged() throws Exception {
        final LocalDateTime currentDateTime = LocalDate.of(2018, 1, 2).atTime(17, 9);
        timeTravelTo(clock, currentDateTime);

        final MindergasnlSettings mindergasnlSettings = new MindergasnlSettings();
        mindergasnlSettings.setAutomatischUploaden(true);
        mindergasnlSettings.setAuthenticatietoken("LetMeIn");
        when(mindergasnlSettingsRepository.findOneByIdIsNotNull()).thenReturn(Optional.of(mindergasnlSettings));

        final BigDecimal yesterdaysGas = new BigDecimal("12412.812");
        final MeterstandOpDag yesterDaysMeterstand = new MeterstandOpDag(currentDateTime.minusDays(1).toLocalDate(), aMeterstand().withGas(yesterdaysGas).build());

        final DatePeriod expectedPeriod = aPeriodWithToDate(currentDateTime.minusDays(1).toLocalDate(), currentDateTime.toLocalDate());
        when(meterstandService.getPerDag(eq(expectedPeriod))).thenReturn(List.of(yesterDaysMeterstand));

        final RuntimeException runtimeException = new RuntimeException("FUBAR");
        when(httpClientBuilderProvider.get()).thenThrow(runtimeException);
        loggingRule.setLevel(Level.ERROR);

        mindergasnlService.uploadMeterstandWhenEnabled();

        final LoggingEvent loggingEvent = loggingRule.getLoggedEventCaptor().getValue();
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo("Failed to upload to mindergas.nl");
        assertThat(loggingEvent.getThrowableProxy().getClassName()).isEqualTo(runtimeException.getClass().getName());
    }
}