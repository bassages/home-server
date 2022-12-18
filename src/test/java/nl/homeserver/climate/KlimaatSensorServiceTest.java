package nl.homeserver.climate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static nl.homeserver.climate.KlimaatSensor.aKlimaatSensor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KlimaatSensorServiceTest {

    private static final String SOME_SENSOR_CODE = "someSensorCode";

    @InjectMocks
    KlimaatSensorService klimaatSensorService;

    @Mock
    KlimaatSensorRepository klimaatSensorRepository;

    @Test
    void whenGetKlimaatSensorByCodeThenDelegatedToRepository() {
        // given
        final KlimaatSensor klimaatSensor = aKlimaatSensor().code(SOME_SENSOR_CODE).build();
        when(klimaatSensorRepository.findFirstByCode(SOME_SENSOR_CODE)).thenReturn(Optional.of(klimaatSensor));

        // when
        final Optional<KlimaatSensor> klimaatSensorsByCode = klimaatSensorService.getByCode(SOME_SENSOR_CODE);

        // then
        assertThat(klimaatSensorsByCode).contains(klimaatSensor);
    }

    @Test
    void whenGetAllThenDelegatedToRepository() {
        // given
        final List<KlimaatSensor> klimaatSensors = List.of(mock(KlimaatSensor.class), mock(KlimaatSensor.class));
        when(klimaatSensorRepository.findAll()).thenReturn(klimaatSensors);

        // when
        final List<KlimaatSensor> all = klimaatSensorService.getAll();

        // then
        assertThat(all).isSameAs(klimaatSensors);
    }

    @Test
    void whenUpdateThenSavedByRepositoryAndReturned() {
        // given
        final KlimaatSensor klimaatSensor = mock(KlimaatSensor.class);
        when(klimaatSensorRepository.save(klimaatSensor)).thenAnswer(AdditionalAnswers.returnsFirstArg());

        // when
        final KlimaatSensor savedKlimaatSensor = klimaatSensorService.save(klimaatSensor);

        // then
        assertThat(savedKlimaatSensor).isSameAs(klimaatSensor);
    }

    @Test
    void whenDeleteThenDeletedByRepository() {
        // given
        final KlimaatSensor klimaatSensor = mock(KlimaatSensor.class);

        // when
        klimaatSensorService.delete(klimaatSensor);

        // then
        verify(klimaatSensorRepository).delete(klimaatSensor);
    }

    @Test
    void givenNotExistingSensorCodeWhenGetOrCreateIfNonExistsThenCreated() {
        // given
        final ArgumentCaptor<KlimaatSensor> klimaatSensorArgumentCaptor = ArgumentCaptor.forClass(KlimaatSensor.class);
        when(klimaatSensorRepository.findFirstByCode(SOME_SENSOR_CODE)).thenReturn(Optional.empty());
        when(klimaatSensorRepository.save(any(KlimaatSensor.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        final KlimaatSensor klimaatSensor = klimaatSensorService.getOrCreateIfNonExists(SOME_SENSOR_CODE);

        // when
        verify(klimaatSensorRepository).save(klimaatSensorArgumentCaptor.capture());

        // then
        final KlimaatSensor capturedKlimaatSensor = klimaatSensorArgumentCaptor.getValue();
        assertThat(capturedKlimaatSensor).isNotNull();
        assertThat(capturedKlimaatSensor.getCode()).isEqualTo(SOME_SENSOR_CODE);
        assertThat(capturedKlimaatSensor.getOmschrijving()).isNull();

        assertThat(klimaatSensor).isEqualTo(capturedKlimaatSensor);
    }

    @Test
    void givenExistingSensorCodeWhenGetOrCreateIfNonExistsThenReturned() {
        // given
        final KlimaatSensor existingKlimaatSensor = aKlimaatSensor().code(SOME_SENSOR_CODE).build();
        when(klimaatSensorRepository.findFirstByCode(SOME_SENSOR_CODE)).thenReturn(Optional.of(existingKlimaatSensor));

        // when
        final KlimaatSensor actuallyReturnedKlimaatSensor = klimaatSensorService.getOrCreateIfNonExists(SOME_SENSOR_CODE);

        // then
        assertThat(actuallyReturnedKlimaatSensor).isSameAs(existingKlimaatSensor);
        verify(klimaatSensorRepository).findFirstByCode(SOME_SENSOR_CODE);
        verifyNoMoreInteractions(klimaatSensorRepository);
    }
}
