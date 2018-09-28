package nl.homeserver.energie.energiecontract;

import static java.time.Month.MARCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnergiecontractControllerTest {

    @InjectMocks
    private EnergiecontractController energiecontractController;

    @Mock
    private EnergiecontractService energiecontractService;

    @Test
    public void givenANewEnergyContractWhenSaveThenDelegatedToService() {
        when(energiecontractService.save(any())).then(returnsFirstArg());
        final EnergiecontractDto energieContractDto = mock(EnergiecontractDto.class);
        when(energieContractDto.getId()).thenReturn(null);

        final Energiecontract savedEnergieContract = energiecontractController.save(energieContractDto);

        verify(energiecontractService).save(any());
    }

    @Test
    public void givenAnExistingEnergyContractWhenSaveThenDelegatedToService() {
        when(energiecontractService.save(any())).then(returnsFirstArg());

        final long id = 13451L;
        final LocalDate validTo = LocalDate.of(2018, MARCH, 13);

        final Energiecontract existingEnergiecontract = mock(Energiecontract.class);

        final EnergiecontractDto energiecontractToUpdate = mock(EnergiecontractDto.class);
        when(energiecontractToUpdate.getId()).thenReturn(id);

        when(energiecontractService.getById(id)).thenReturn(existingEnergiecontract);

        final Energiecontract savedEnergieContract = energiecontractController.save(energiecontractToUpdate);

        assertThat(savedEnergieContract).isSameAs(existingEnergiecontract);
    }

    @Test
    public void whenDeleteThenDelegatedToService() {
        final long id = 1234L;

        energiecontractController.delete(id);

        verify(energiecontractService).delete(id);
    }

    @Test
    public void whenGetAllThenDelegatedToService() {
        final List<Energiecontract> allEnergieContracts = List.of(mock(Energiecontract.class), mock(Energiecontract.class));
        when(energiecontractService.getAll()).thenReturn(allEnergieContracts);

        assertThat(energiecontractController.getAll()).isSameAs(allEnergieContracts);
    }

    @Test
    public void whenGetCurrentlyValidEnergiecontractThenDelegatedToService() {
        final Energiecontract currentlyValid = mock(Energiecontract.class);
        when(energiecontractService.getCurrent()).thenReturn(currentlyValid);

        assertThat(energiecontractController.getCurrentlyValid()).isSameAs(currentlyValid);
    }
}