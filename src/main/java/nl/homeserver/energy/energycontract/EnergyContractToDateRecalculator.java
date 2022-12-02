package nl.homeserver.energy.energycontract;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

@Service
@RequiredArgsConstructor
class EnergyContractToDateRecalculator {
    private static final Sort ORDER_BY_VALID_FROM_ASC = Sort.by(Sort.Direction.ASC, "validFrom");

    private final EnergyContractRepository energyContractRepository;

    void recalculate() {
        final List<EnergyContract> energyContracts = energyContractRepository.findAll(ORDER_BY_VALID_FROM_ASC);

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
