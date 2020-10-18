package nl.homeserver.klimaat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static nl.homeserver.klimaat.KlimaatSensorBuilder.aKlimaatSensor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KlimaatSensorServiceTest {

    private static final String SOME_SENSOR_CODE = "someSensorCode";

    @InjectMocks
    private KlimaatSensorService klimaatSensorService;

    @Mock
    private KlimaatSensorRepository klimaatSensorRepository;

    @Test
    public void whenGetKlimaatSensorByCodeThenDelegatedToRepository() {
        // given
        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode(SOME_SENSOR_CODE).build();
        when(klimaatSensorRepository.findFirstByCode(SOME_SENSOR_CODE)).thenReturn(Optional.of(klimaatSensor));

        // when
        final Optional<KlimaatSensor> klimaatSensorsByCode = klimaatSensorService.getByCode(SOME_SENSOR_CODE);

        // then
        assertThat(klimaatSensorsByCode).contains(klimaatSensor);
    }

    @Test
    public void whenGetAllThenDelegatedToRepository() {
        // given
        final List<KlimaatSensor> klimaatSensors = List.of(mock(KlimaatSensor.class), mock(KlimaatSensor.class));
        when(klimaatSensorRepository.findAll()).thenReturn(klimaatSensors);

        // when
        final List<KlimaatSensor> all = klimaatSensorService.getAll();

        // then
        assertThat(all).isSameAs(klimaatSensors);
    }

    @Test
    public void whenUpdateThenSavedByRepositoryAndReturned() {
        // given
        final KlimaatSensor klimaatSensor = mock(KlimaatSensor.class);
        when(klimaatSensorRepository.save(klimaatSensor)).thenAnswer(AdditionalAnswers.returnsFirstArg());

        // when
        final KlimaatSensor savedKlimaatSensor = klimaatSensorService.save(klimaatSensor);

        // then
        assertThat(savedKlimaatSensor).isSameAs(klimaatSensor);
    }

    @Test
    public void whenDeleteThenDeletedByRepository() {
        // given
        final KlimaatSensor klimaatSensor = mock(KlimaatSensor.class);

        // when
        klimaatSensorService.delete(klimaatSensor);

        // then
        verify(klimaatSensorRepository).delete(klimaatSensor);
    }

    @Test
    public void givenNotExistingSensorCodeWhenGetOrCreateIfNonExistsThenCreated() {
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
    public void givenExistingSensorCodeWhenGetOrCreateIfNonExistsThenReturned() {
        // given
        final KlimaatSensor existingKlimaatSensor = aKlimaatSensor().withCode(SOME_SENSOR_CODE).build();
        when(klimaatSensorRepository.findFirstByCode(SOME_SENSOR_CODE)).thenReturn(Optional.of(existingKlimaatSensor));

        // when
        final KlimaatSensor actuallyReturnedKlimaatSensor = klimaatSensorService.getOrCreateIfNonExists(SOME_SENSOR_CODE);

        // then
        assertThat(actuallyReturnedKlimaatSensor).isSameAs(existingKlimaatSensor);
        verify(klimaatSensorRepository).findFirstByCode(SOME_SENSOR_CODE);
        verifyNoMoreInteractions(klimaatSensorRepository);
    }
}
