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

    private final EnergyContractToDateRecalculator energyContractToDateRecalculator;
    private final EnergyContractRepository energyContractRepository;
    private final CacheService cacheService;
    private final Clock clock;

    public List<EnergyContract> getAll() {
        return energyContractRepository.findAll();
    }

    public EnergyContract getCurrent() {
        final LocalDateTime now = now(clock);
        return energyContractRepository.findFirstByValidFromLessThanEqualOrderByValidFromDesc(now.toLocalDate());
    }

    public EnergyContract save(final EnergyContract energyContract) {
        final EnergyContract savedEnergyContract = energyContractRepository.save(energyContract);
        energyContractToDateRecalculator.recalculate();
        cacheService.clearAll();
        return savedEnergyContract;
    }

    public void delete(final long id) {
        energyContractRepository.deleteById(id);
        energyContractToDateRecalculator.recalculate();
        cacheService.clearAll();
    }

    @Cacheable(cacheNames = CACHE_NAME_ENERGY_CONTRACTS_IN_PERIOD)
    public List<EnergyContract> findAllInInPeriod(final DateTimePeriod period) {
        return energyContractRepository.findValidInPeriod(
                period.getFromDateTime().toLocalDate(), period.getToDateTime().toLocalDate());
    }

    public EnergyContract getById(final long id) {
        return energyContractRepository.getById(id);
    }
}
