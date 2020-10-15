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

@Service
@AllArgsConstructor
public class EnergycontractService {

    private static final String CACHE_NAME_ENERGIECONTRACTEN_IN_PERIOD = "energiecontractenInPeriod";

    private final EnergycontractToDateRecalculator energycontractToDateRecalculator;
    private final EnergiecontractRepository energiecontractRepository;
    private final CacheService cacheService;
    private final Clock clock;

    List<Energycontract> getAll() {
        return energiecontractRepository.findAll();
    }

    Energycontract getCurrent() {
        final LocalDateTime now = now(clock);
        return energiecontractRepository.findFirstByValidFromLessThanEqualOrderByValidFromDesc(now.toLocalDate());
    }

    Energycontract save(final Energycontract energycontract) {
        final Energycontract savedEnergieContract = energiecontractRepository.save(energycontract);
        energycontractToDateRecalculator.recalculate();
        cacheService.clearAll();
        return savedEnergieContract;
    }

    void delete(final long id) {
        energiecontractRepository.deleteById(id);
        energycontractToDateRecalculator.recalculate();
        cacheService.clearAll();
    }

    @Cacheable(cacheNames = CACHE_NAME_ENERGIECONTRACTEN_IN_PERIOD)
    public List<Energycontract> findAllInInPeriod(final DateTimePeriod period) {
        return energiecontractRepository.findValidInPeriod(period.getFromDateTime().toLocalDate(), period.getToDateTime().toLocalDate());
    }

    Energycontract getById(final long id) {
        return energiecontractRepository.getOne(id);
    }
}
