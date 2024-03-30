package nl.homeserver.climate;

import nl.homeserver.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.now;
import static java.time.Month.JANUARY;
import static java.util.Optional.empty;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.climate.SensorType.LUCHTVOCHTIGHEID;
import static nl.homeserver.climate.SensorType.TEMPERATUUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KlimaatControllerTest {
    private static final String EXISTING_SENSOR_CODE = "LIVINGROOM";
    private static final String NOT_EXISTING_SENSOR_CODE = "DOES_NOT_EXISTS";
    private static final String EXPECTED_MESSAGE_WHEN_KLIMAATSENSOR_DOES_NOT_EXIST = "KlimaatSensor [" + NOT_EXISTING_SENSOR_CODE + "] does not exist";

    @InjectMocks
    KlimaatController klimaatController;

    @Mock
    KlimaatService klimaatService;
    @Mock
    KlimaatSensorService klimaatSensorService;
    @Mock
    IncomingKlimaatService incomingKlimaatService;

    @Mock
    KlimaatSensor klimaatSensor;

    @Captor
    ArgumentCaptor<Klimaat> klimaatCaptor;

    @Test
    void whenAddThenKlimaatSensorSetAndDelegatedToIncomingKlimaatService() {
        final String sensorCode = "LIVINGROOM";
        when(klimaatSensorService.getByCode(sensorCode)).thenReturn(Optional.of(klimaatSensor));

        final KlimaatDto klimaatDto = new KlimaatDto(1, now().atStartOfDay(),
                new BigDecimal("12.67"), new BigDecimal("60.2"));

        klimaatController.add(sensorCode, klimaatDto);

        verify(incomingKlimaatService).add(klimaatCaptor.capture());
        assertThat(klimaatCaptor.getValue().getKlimaatSensor()).isSameAs(klimaatSensor);
        assertThat(klimaatCaptor.getValue().getTemperatuur()).isEqualTo(klimaatDto.temperatuur());
        assertThat(klimaatCaptor.getValue().getLuchtvochtigheid()).isEqualTo(klimaatDto.luchtvochtigheid());
    }

    @Test
    void givenNoKlimaatSensorExistsWithGivenCodeWhenAddThenExceptionIsThrown() {
        when(klimaatSensorService.getByCode(NOT_EXISTING_SENSOR_CODE)).thenReturn(empty());

        final KlimaatDto klimaatDto = mock(KlimaatDto.class);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> klimaatController.add(NOT_EXISTING_SENSOR_CODE, klimaatDto))
                .withMessage(EXPECTED_MESSAGE_WHEN_KLIMAATSENSOR_DOES_NOT_EXIST);
    }

    @Test
    void whenGetMostRecentThenDelegatedToIncomingKlimaatService() {
        when(klimaatSensorService.getByCode(EXISTING_SENSOR_CODE)).thenReturn(Optional.of(klimaatSensor));

        final RealtimeKlimaat realtimeKlimaat = mock(RealtimeKlimaat.class);
        when(incomingKlimaatService.getMostRecent(EXISTING_SENSOR_CODE)).thenReturn(realtimeKlimaat);

        assertThat(klimaatController.getMostRecent(EXISTING_SENSOR_CODE)).isSameAs(realtimeKlimaat);
    }

    @Test
    void givenNoKlimaatSensorExistsWithGivenCodeWhenGetMostRecentThenExceptionIsThrown() {
        when(klimaatSensorService.getByCode(NOT_EXISTING_SENSOR_CODE)).thenReturn(empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> klimaatController.getMostRecent(NOT_EXISTING_SENSOR_CODE))
                .withMessage(EXPECTED_MESSAGE_WHEN_KLIMAATSENSOR_DOES_NOT_EXIST);
    }

    @Test
    void whenGetHighestThenDelegatedToService() {
        when(klimaatSensorService.getByCode(EXISTING_SENSOR_CODE)).thenReturn(Optional.of(klimaatSensor));

        final List<Klimaat> highestKlimaats = List.of(mock(Klimaat.class), mock(Klimaat.class));

        final SensorType sensorType = TEMPERATUUR;
        final LocalDate from = LocalDate.of(2018, JANUARY, 12);
        final LocalDate to = LocalDate.of(2018, JANUARY, 27);
        final int limit = 20;

        when(klimaatService.getHighest(EXISTING_SENSOR_CODE, sensorType,
                                       aPeriodWithToDate(from, to), limit))
                            .thenReturn(highestKlimaats);

        assertThat(klimaatController.getHighest(EXISTING_SENSOR_CODE, sensorType.name(), from, to, limit))
                                    .isSameAs(highestKlimaats);
    }

    @Test
    void givenNoKlimaatSensorExistsWithGivenCodeWhenGetHighestThenExceptionIsThrown() {
        when(klimaatSensorService.getByCode(NOT_EXISTING_SENSOR_CODE)).thenReturn(empty());

        final LocalDate now = now();
        final String sensorType = TEMPERATUUR.name();

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> klimaatController.getHighest(NOT_EXISTING_SENSOR_CODE, sensorType, now, now, 1234))
                .withMessage(EXPECTED_MESSAGE_WHEN_KLIMAATSENSOR_DOES_NOT_EXIST);
    }

    @Test
    void whenGetLowestThenDelegatedToService() {
        when(klimaatSensorService.getByCode(EXISTING_SENSOR_CODE)).thenReturn(Optional.of(klimaatSensor));

        final List<Klimaat> higestKlimaats = List.of(mock(Klimaat.class), mock(Klimaat.class));

        final SensorType sensorType = TEMPERATUUR;
        final LocalDate from = LocalDate.of(2018, JANUARY, 12);
        final LocalDate to = LocalDate.of(2018, JANUARY, 27);
        final int limit = 20;

        when(klimaatService.getLowest(EXISTING_SENSOR_CODE, sensorType, aPeriodWithToDate(from, to), limit)).thenReturn(higestKlimaats);

        assertThat(klimaatController.getLowest(EXISTING_SENSOR_CODE, sensorType.name(), from, to, limit)).isSameAs(higestKlimaats);
    }

    @Test
    void givenNoKlimaatSensorExistsWithGivenCodeWhenGetLowestThenExceptionIsThrown() {
        when(klimaatSensorService.getByCode(NOT_EXISTING_SENSOR_CODE)).thenReturn(empty());

        final LocalDate now = now();
        final String sensorType = TEMPERATUUR.name();

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> klimaatController.getLowest(NOT_EXISTING_SENSOR_CODE, sensorType, now, now, 1234))
                .withMessage(EXPECTED_MESSAGE_WHEN_KLIMAATSENSOR_DOES_NOT_EXIST);
    }

    @Test
    void whenGetAveragePerMonthThenDelegatedToService() {
        final SensorType sensorType = LUCHTVOCHTIGHEID;
        final int[] years = new int[] {2017, 2018};

        when(klimaatSensorService.getByCode(EXISTING_SENSOR_CODE)).thenReturn(Optional.of(klimaatSensor));

        final GemiddeldeKlimaatPerMaand month1 = new GemiddeldeKlimaatPerMaand(LocalDate.now(), null);
        final GemiddeldeKlimaatPerMaand month2 = new GemiddeldeKlimaatPerMaand(LocalDate.now(), null);
        final List<GemiddeldeKlimaatPerMaand> year = List.of(month1, month2);

        final List<List<GemiddeldeKlimaatPerMaand>> monthsInYears = List.of(year);
        when(klimaatService.getAveragePerMonthInYears(EXISTING_SENSOR_CODE, sensorType, years)).thenReturn(monthsInYears);

        assertThat(klimaatController.getAverage(EXISTING_SENSOR_CODE, sensorType.name(), years)).isSameAs(monthsInYears);
    }

    @Test
    void whenFindAllInPeriodThenDelegatedToSerice() {
        final LocalDate from = LocalDate.of(2018, JANUARY, 12);
        final LocalDate to = LocalDate.of(2018, JANUARY, 27);

        when(klimaatSensorService.getByCode(EXISTING_SENSOR_CODE)).thenReturn(Optional.of(klimaatSensor));

        final List<Klimaat> allInPeriod = List.of(mock(Klimaat.class), mock(Klimaat.class));

        when(klimaatService.getInPeriod(EXISTING_SENSOR_CODE, aPeriodWithToDate(from, to))).thenReturn(allInPeriod);

        assertThat(klimaatController.findAllInPeriod(EXISTING_SENSOR_CODE, from, to)).isSameAs(allInPeriod);
    }

    @Test
    void givenNoKlimaatSensorExistsWithGivenCodeWhenFindAllInPeriodThenExceptionIsThrown() {
        when(klimaatSensorService.getByCode(NOT_EXISTING_SENSOR_CODE)).thenReturn(empty());

        final LocalDate now = now();

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> klimaatController.findAllInPeriod(NOT_EXISTING_SENSOR_CODE, now, now))
                .withMessage(EXPECTED_MESSAGE_WHEN_KLIMAATSENSOR_DOES_NOT_EXIST);
    }

    @Test
    void whenGetAllKlimaatSensorsThenDelegatedToService() {
        final List<KlimaatSensor> allKlimaatSensors = List.of(klimaatSensor);
        when(klimaatSensorService.getAll()).thenReturn(allKlimaatSensors);

        assertThat(klimaatController.getAllKlimaatSensors()).isEqualTo(allKlimaatSensors);
    }

    @Test
    void givenExistingKlimaatSensorCodeWhenUpdateThenUpdatedByServiceAndReturned() {
        final KlimaatSensor existingKlimaatSensor = mock(KlimaatSensor.class);

        final String omschrijving = "The new Description";
        final KlimaatSensorDto klimaatSensorDto = new KlimaatSensorDto();
        klimaatSensorDto.setOmschrijving(omschrijving);

        when(klimaatSensorService.getByCode(EXISTING_SENSOR_CODE)).thenReturn(Optional.of(existingKlimaatSensor));

        final KlimaatSensor updatedKlimaatSensor = mock(KlimaatSensor.class);
        when(klimaatSensorService.save(existingKlimaatSensor)).thenReturn(updatedKlimaatSensor);

        assertThat(klimaatController.update(EXISTING_SENSOR_CODE, klimaatSensorDto)).isSameAs(updatedKlimaatSensor);

        verify(existingKlimaatSensor).setOmschrijving(omschrijving);
    }

    @Test
    void givenNonExistingKlimaatSensorCodeWhenUpdateThenException() {
        when(klimaatSensorService.getByCode(NOT_EXISTING_SENSOR_CODE)).thenReturn(Optional.empty());

        final KlimaatSensorDto klimaatSensorDto = mock(KlimaatSensorDto.class);

        assertThatExceptionOfType(ResourceNotFoundException.class)
            .isThrownBy(() -> klimaatController.update(NOT_EXISTING_SENSOR_CODE, klimaatSensorDto))
            .withMessage("KlimaatSensor [DOES_NOT_EXISTS] does not exist");
    }

    @Test
    void givenExistingKlimaatSensorCodeWhenDeleteThenDeletedByServices() {
        when(klimaatSensor.getCode()).thenReturn(EXISTING_SENSOR_CODE);
        when(klimaatSensorService.getByCode(EXISTING_SENSOR_CODE)).thenReturn(Optional.of(klimaatSensor));

        klimaatController.delete(klimaatSensor.getCode());

        final InOrder inOrder = inOrder(klimaatService, klimaatSensorService);
        inOrder.verify(klimaatSensorService).getByCode(EXISTING_SENSOR_CODE);
        inOrder.verify(klimaatService).deleteByClimateSensor(klimaatSensor);
        inOrder.verify(klimaatSensorService).delete(klimaatSensor);
    }

    @Test
    void givenNonExistingKlimaatSensorCodeWheDeleteThenException() {
        when(klimaatSensor.getCode()).thenReturn(NOT_EXISTING_SENSOR_CODE);
        when(klimaatSensorService.getByCode(NOT_EXISTING_SENSOR_CODE)).thenReturn(Optional.empty());

        final String code = klimaatSensor.getCode();

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> klimaatController.delete(code))
                .withMessage("KlimaatSensor [DOES_NOT_EXISTS] does not exist");
    }
}
