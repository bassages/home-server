package nl.homeserver.energiecontract;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class EnergiecontractToDateRecalculator {

    private final EnergiecontractRepository energiecontractRepository;

    public EnergiecontractToDateRecalculator(EnergiecontractRepository energiecontractRepository) {
        this.energiecontractRepository = energiecontractRepository;
    }

    public void recalculate() {
        Sort sortByVanAsc = new Sort(Sort.Direction.ASC, "validFrom");

        List<Energiecontract> energiecontracts = energiecontractRepository.findAll(sortByVanAsc);

        Energiecontract previousEnergiecontract = null;
        for (int i = 0; i < energiecontracts.size(); i++) {
            Energiecontract currentEnergiecontract = energiecontracts.get(i);

            if (previousEnergiecontract != null) {
                LocalDate validTo = currentEnergiecontract.getValidFrom();
                if (notEqual(previousEnergiecontract.getValidTo(), validTo)) {
                    previousEnergiecontract.setValidTo(validTo);
                    energiecontractRepository.save(previousEnergiecontract);
                }
            }

            if (i == (energiecontracts.size() - 1) && currentEnergiecontract.getValidTo() != null) {
                currentEnergiecontract.setValidTo(null);
                energiecontractRepository.save(currentEnergiecontract);
            }
            previousEnergiecontract = currentEnergiecontract;
        }
    }
}
