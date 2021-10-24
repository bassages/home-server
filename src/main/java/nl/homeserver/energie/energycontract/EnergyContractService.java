package nl.homeserver.energie.energycontract;

import static java.time.LocalDateTime.now;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.homeserver.DateTimePeriod;
import nl.homeserver.cache.CacheService;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class EnergyContractService {

    private static final String CACHE_NAME_ENERGY_CONTRACTS_IN_PERIOD = "energyContractsInPeriod";

    private final EnergycontractToDateRecalculator energycontractToDateRecalculator;
    private final EnergycontractRepository energycontractRepository;
    private final CacheService cacheService;
    private final Clock clock;

    public List<Energycontract> getAll() {
        return energycontractRepository.findAll();
    }

    public Energycontract getCurrent() {
        final LocalDateTime now = now(clock);
        return energycontractRepository.findFirstByValidFromLessThanEqualOrderByValidFromDesc(now.toLocalDate());
    }

    public Energycontract save(final Energycontract energycontract) {
        final Energycontract savedEnergieContract = energycontractRepository.save(energycontract);
        energycontractToDateRecalculator.recalculate();
        cacheService.clearAll();
        return savedEnergieContract;
    }

    public void delete(final long id) {
        energycontractRepository.deleteById(id);
        energycontractToDateRecalculator.recalculate();
        cacheService.clearAll();
    }

    @Cacheable(cacheNames = CACHE_NAME_ENERGY_CONTRACTS_IN_PERIOD)
    public List<Energycontract> findAllInInPeriod(final DateTimePeriod period) {
        return energycontractRepository.findValidInPeriod(
                period.getFromDateTime().toLocalDate(), period.getToDateTime().toLocalDate());
    }

    public Energycontract getById(final long id) {
        return energycontractRepository.getById(id);
    }
}
