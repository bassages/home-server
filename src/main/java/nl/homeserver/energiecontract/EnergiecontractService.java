package nl.homeserver.energiecontract;

import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.ObjectUtils.notEqual;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import nl.homeserver.DateTimePeriod;
import nl.homeserver.cache.CacheService;

@Service
public class EnergiecontractService {

    private static final String CACHE_NAME_ENERGIECONTRACTEN_IN_PERIOD = "energiecontractenInPeriod";

    private final EnergiecontractRepository energiecontractRepository;
    private final CacheService cacheService;
    private final Clock clock;

    public EnergiecontractService(EnergiecontractRepository energiecontractRepository, CacheService cacheService, Clock clock) {
        this.energiecontractRepository = energiecontractRepository;
        this.cacheService = cacheService;
        this.clock = clock;
    }

    public List<Energiecontract> getAll() {
        return energiecontractRepository.findAll();
    }

    public Energiecontract getCurrent() {
        LocalDateTime now = now(clock);
        return energiecontractRepository.findFirstByValidFromLessThanEqualOrderByValidFromDesc(now.toLocalDate());
    }

    public Energiecontract save(Energiecontract energiecontract) {
        Energiecontract result = energiecontractRepository.save(energiecontract);
        recalculateValidTo();
        cacheService.clearAll();
        return result;
    }

    public void delete(long id) {
        energiecontractRepository.delete(id);
        recalculateValidTo();
        cacheService.clearAll();
    }

    protected void recalculateValidTo() {
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

    @Cacheable(cacheNames = CACHE_NAME_ENERGIECONTRACTEN_IN_PERIOD)
    public List<Energiecontract> findAllInInPeriod(DateTimePeriod period) {
        return energiecontractRepository.findValidInPeriod(period.getFromDateTime().toLocalDate(), period.getToDateTime().toLocalDate());
    }

    public Energiecontract getById(long id) {
        return energiecontractRepository.getOne(id);
    }
}
