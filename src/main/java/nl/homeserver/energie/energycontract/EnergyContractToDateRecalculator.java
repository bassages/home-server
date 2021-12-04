package nl.homeserver.energie.energycontract;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
class EnergyContractToDateRecalculator {

    private final EnergyContractRepository energyContractRepository;

    void recalculate() {
        Sort ordering = Sort.by(Sort.Direction.ASC, "validFrom");
        final List<EnergyContract> energyContracts = energyContractRepository.findAll(ordering);

        EnergyContract previousEnergyContract = null;
        for (int i = 0; i < energyContracts.size(); i++) {
            final EnergyContract currentEnergyContract = energyContracts.get(i);

            if (previousEnergyContract != null) {
                final LocalDate validTo = currentEnergyContract.getValidFrom();
                if (notEqual(previousEnergyContract.getValidTo(), validTo)) {
                    previousEnergyContract.setValidTo(validTo);
                    energyContractRepository.save(previousEnergyContract);
                }
            }

            if (i == (energyContracts.size() - 1) && currentEnergyContract.getValidTo() != null) {
                currentEnergyContract.setValidTo(null);
                energyContractRepository.save(currentEnergyContract);
            }
            previousEnergyContract = currentEnergyContract;
        }
    }
}
