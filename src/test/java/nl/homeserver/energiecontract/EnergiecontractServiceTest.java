package nl.homeserver.energiecontract;

import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;

@RunWith(MockitoJUnitRunner.class)
public class EnergiecontractServiceTest {

    @InjectMocks
    private EnergiecontractService energiecontractService;

    @Mock
    private EnergiecontractRepository energiecontractRepository;
    @Mock
    private Clock clock;

    @Test
    public void givenMultipleEnergieContractsWithoutValidToSetWhenRecalculateValidToThenSetAndSaved() {
        Energiecontract energiecontract1 = new Energiecontract();
        energiecontract1.setValidFrom(LocalDate.of(2017, JANUARY, 1));

        Energiecontract energiecontract2 = new Energiecontract();
        energiecontract2.setValidFrom(LocalDate.of(2017, JANUARY, 6));

        Energiecontract energiecontract3 = new Energiecontract();
        energiecontract3.setValidFrom(LocalDate.of(2017, JANUARY, 12));

        when(energiecontractRepository.findAll(any(Sort.class))).thenReturn(asList(energiecontract1, energiecontract2, energiecontract3));

        energiecontractService.recalculateValidTo();

        assertThat(energiecontract1.getValidTo()).isEqualTo(energiecontract2.getValidFrom());
        assertThat(energiecontract2.getValidTo()).isEqualTo(energiecontract3.getValidFrom());
        assertThat(energiecontract3.getValidTo()).isNull();

        verify(energiecontractRepository).save(energiecontract1);
        verify(energiecontractRepository).save(energiecontract2);
    }

    @Test
    public void recalculateTotEnMetWithoutChanges() {
        Energiecontract energiecontract1 = new Energiecontract();
        energiecontract1.setValidFrom(LocalDate.of(2017, JANUARY, 1));
        energiecontract1.setValidTo(LocalDate.of(2017, JANUARY, 5));

        Energiecontract energiecontract2 = new Energiecontract();
        energiecontract2.setValidFrom(energiecontract1.getValidTo());
        energiecontract2.setValidTo(LocalDate.of(2017, JANUARY, 14));

        Energiecontract energiecontract3 = new Energiecontract();
        energiecontract3.setValidFrom(energiecontract2.getValidTo());
        energiecontract3.setValidTo(null);

        when(energiecontractRepository.findAll(any(Sort.class))).thenReturn(asList(energiecontract1, energiecontract2, energiecontract3));

        energiecontractService.recalculateValidTo();

        assertThat(energiecontract1.getValidTo()).isEqualTo(LocalDate.of(2017, JANUARY, 5));
        assertThat(energiecontract2.getValidTo()).isEqualTo(LocalDate.of(2017, JANUARY, 14));
        assertThat(energiecontract3.getValidTo()).isNull();

        verify(energiecontractRepository, never()).save(any(Energiecontract.class));
    }

    @Test
    public void whenGetByIdThenDelegatedToRepository() {
        long id = 132L;
        Energiecontract energiecontract = mock(Energiecontract.class);
        when(energiecontractRepository.getOne(id)).thenReturn(energiecontract);

        assertThat(energiecontractService.getById(id)).isSameAs(energiecontract);
    }

    @Test
    public void whenGetAllThenDelegatedToRepository() {
        List<Energiecontract> all = asList(mock(Energiecontract.class), mock(Energiecontract.class));
        when(energiecontractRepository.findAll()).thenReturn(all);

        assertThat(energiecontractService.getAll()).isSameAs(all);
    }

    @Test
    public void whenGetCurrentThenDelegatedToRepository() {
        LocalDate today = LocalDate.of(2019, JANUARY, 1);
        timeTravelTo(clock, today.atStartOfDay());

        Energiecontract energiecontract = mock(Energiecontract.class);
        when(energiecontractRepository.findFirstByValidFromLessThanEqualOrderByValidFromDesc(today)).thenReturn(energiecontract);

        assertThat(energiecontractService.getCurrent()).isSameAs(energiecontract);
    }
}