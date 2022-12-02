package nl.homeserver.energy.energycontract;

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
class EnergyContractControllerTest {

    @InjectMocks
    EnergyContractController energyContractController;

    @Mock
    EnergyContractService energyContractService;

    @Test
    void givenANewEnergyContractWhenSaveThenDelegatedToService() {
        when(energyContractService.save(any())).then(returnsFirstArg());
        final EnergyContractDto energyContractDto = mock(EnergyContractDto.class);
        when(energyContractDto.id()).thenReturn(null);

        final EnergyContract savedEnergyContract = energyContractController.save(energyContractDto);

        assertThat(savedEnergyContract).isNotNull();
        verify(energyContractService).save(any());
    }

    @Test
    void givenAnExistingEnergyContractWhenSaveThenDelegatedToService() {
        when(energyContractService.save(any())).then(returnsFirstArg());

        final long id = 13451L;

        final EnergyContract existingEnergyContract = mock(EnergyContract.class);

        final EnergyContractDto energiecontractToUpdate = mock(EnergyContractDto.class);
        when(energiecontractToUpdate.id()).thenReturn(id);

        when(energyContractService.getById(id)).thenReturn(existingEnergyContract);

        final EnergyContract savedEnergieContract = energyContractController.save(energiecontractToUpdate);

        assertThat(savedEnergieContract).isSameAs(existingEnergyContract);
    }

    @Test
    void whenDeleteThenDelegatedToService() {
        final long id = 1234L;

        energyContractController.delete(id);

        verify(energyContractService).delete(id);
    }

    @Test
    void whenGetAllThenDelegatedToService() {
        final List<EnergyContract> allEnergieContracts = List.of(mock(EnergyContract.class), mock(EnergyContract.class));
        when(energyContractService.getAll()).thenReturn(allEnergieContracts);

        assertThat(energyContractController.getAll()).isSameAs(allEnergieContracts);
    }

    @Test
    void whenGetCurrentlyValidEnergiecontractThenDelegatedToService() {
        final EnergyContract currentlyValid = mock(EnergyContract.class);
        when(energyContractService.getCurrent()).thenReturn(currentlyValid);

        assertThat(energyContractController.getCurrentlyValid()).isSameAs(currentlyValid);
    }
}
