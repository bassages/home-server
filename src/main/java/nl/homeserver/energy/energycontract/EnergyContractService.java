package nl.homeserver.energy.energycontract;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nl.homeserver.DatePeriod;
import nl.homeserver.cache.CacheService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;

@Service
@Transactional
@RequiredArgsConstructor
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
    public List<EnergyContract> findAllInInPeriod(final DatePeriod period) {
        return energyContractRepository.findValidInPeriod(period.getFromDate(), period.getToDate());
    }

    public EnergyContract getById(final long id) {
        return energyContractRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No energy contract found with id " + id));
    }
}
