package nl.homeserver.energie.energycontract;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
class EnergycontractToDateRecalculator {

    private final EnergycontractRepository energycontractRepository;

    void recalculate() {
        Sort ordering = Sort.by(Sort.Direction.ASC, "validFrom");
        final List<Energycontract> energycontracts = energycontractRepository.findAll(ordering);

        Energycontract previousEnergycontract = null;
        for (int i = 0; i < energycontracts.size(); i++) {
            final Energycontract currentEnergycontract = energycontracts.get(i);

            if (previousEnergycontract != null) {
                final LocalDate validTo = currentEnergycontract.getValidFrom();
                if (notEqual(previousEnergycontract.getValidTo(), validTo)) {
                    previousEnergycontract.setValidTo(validTo);
                    energycontractRepository.save(previousEnergycontract);
                }
            }

            if (i == (energycontracts.size() - 1) && currentEnergycontract.getValidTo() != null) {
                currentEnergycontract.setValidTo(null);
                energycontractRepository.save(currentEnergycontract);
            }
            previousEnergycontract = currentEnergycontract;
        }
    }
}
