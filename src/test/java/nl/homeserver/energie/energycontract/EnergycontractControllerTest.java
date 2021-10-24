package nl.homeserver.energie.energycontract;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnergycontractControllerTest {

    @InjectMocks
    EnergycontractController energycontractController;

    @Mock
    EnergyContractService energycontractService;

    @Test
    void givenANewEnergyContractWhenSaveThenDelegatedToService() {
        when(energycontractService.save(any())).then(returnsFirstArg());
        final EnergycontractDto energieContractDto = mock(EnergycontractDto.class);
        when(energieContractDto.getId()).thenReturn(null);

        final Energycontract savedEnergieContract = energycontractController.save(energieContractDto);

        assertThat(savedEnergieContract).isNotNull();
        verify(energycontractService).save(any());
    }

    @Test
    void givenAnExistingEnergyContractWhenSaveThenDelegatedToService() {
        when(energycontractService.save(any())).then(returnsFirstArg());

        final long id = 13451L;

        final Energycontract existingEnergycontract = mock(Energycontract.class);

        final EnergycontractDto energiecontractToUpdate = mock(EnergycontractDto.class);
        when(energiecontractToUpdate.getId()).thenReturn(id);

        when(energycontractService.getById(id)).thenReturn(existingEnergycontract);

        final Energycontract savedEnergieContract = energycontractController.save(energiecontractToUpdate);

        assertThat(savedEnergieContract).isSameAs(existingEnergycontract);
    }

    @Test
    void whenDeleteThenDelegatedToService() {
        final long id = 1234L;

        energycontractController.delete(id);

        verify(energycontractService).delete(id);
    }

    @Test
    void whenGetAllThenDelegatedToService() {
        final List<Energycontract> allEnergieContracts = List.of(mock(Energycontract.class), mock(Energycontract.class));
        when(energycontractService.getAll()).thenReturn(allEnergieContracts);

        assertThat(energycontractController.getAll()).isSameAs(allEnergieContracts);
    }

    @Test
    void whenGetCurrentlyValidEnergiecontractThenDelegatedToService() {
        final Energycontract currentlyValid = mock(Energycontract.class);
        when(energycontractService.getCurrent()).thenReturn(currentlyValid);

        assertThat(energycontractController.getCurrentlyValid()).isSameAs(currentlyValid);
    }
}
