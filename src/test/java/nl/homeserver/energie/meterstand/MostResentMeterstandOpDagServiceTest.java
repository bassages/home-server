package nl.homeserver.energie.meterstand;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MostResentMeterstandOpDagServiceTest {

    @InjectMocks
    MostResentMeterstandOpDagService mostResentMeterstandOpDagService;

    @Mock
    MeterstandRepository meterstandRepository;

    @Test
    void whenGetPerDagForTodayThenNotCachedMeterstandReturned() {
        // given
        final LocalDate day = LocalDate.of(2017, JANUARY, 13);

        final Meterstand meestRecenteMeterstandOpDag = mock(Meterstand.class);
        when(meterstandRepository.findMostRecentInPeriod(day.atStartOfDay(), day.plusDays(1).atStartOfDay().minusNanos(1)))
                .thenReturn(Optional.of(meestRecenteMeterstandOpDag));

        // when
        final Optional<Meterstand> meterstandPerDag = mostResentMeterstandOpDagService
                .getPotentiallyCachedMeestRecenteMeterstandOpDag(day);

        // then
        assertThat(meterstandPerDag).contains(meestRecenteMeterstandOpDag);
    }

}
