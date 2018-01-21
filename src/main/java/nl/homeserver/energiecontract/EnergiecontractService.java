package nl.homeserver.energiecontract;

import static java.time.LocalDateTime.now;
import static nl.homeserver.DateTimeUtil.toMillisSinceEpoch;
import static org.apache.commons.lang3.ObjectUtils.notEqual;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import nl.homeserver.DateTimePeriod;
import nl.homeserver.cache.CacheService;

@Service
public class EnergiecontractService {

    public static final long SINT_JUTTEMIS = 7258114800000L;

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
        return energiecontractRepository.findFirstByVanLessThanEqualOrderByVanDesc(toMillisSinceEpoch(now));
    }

    public Energiecontract save(Energiecontract energiecontract) {
        if (energiecontract.getTotEnMet() == null) {
            energiecontract.setTotEnMet(0L);
        }
        Energiecontract result = energiecontractRepository.save(energiecontract);
        recalculateTotEnMet();
        cacheService.clearAll();
        return result;
    }

    public void delete(long id) {
        energiecontractRepository.delete(id);
        recalculateTotEnMet();
        cacheService.clearAll();
    }

    protected void recalculateTotEnMet() {
        Sort sortByVanAsc = new Sort(Sort.Direction.ASC, "van");

        List<Energiecontract> energiecontractList = energiecontractRepository.findAll(sortByVanAsc);

        Energiecontract previousEnergiecontract = null;
        for (int i = 0; i < energiecontractList.size(); i++) {
            Energiecontract currentEnergiecontract = energiecontractList.get(i);
            if (previousEnergiecontract != null) {
                long totEnMet = currentEnergiecontract.getVan() - 1;
                if (notEqual(previousEnergiecontract.getTotEnMet(), totEnMet)) {
                    previousEnergiecontract.setTotEnMet(totEnMet);
                    energiecontractRepository.save(previousEnergiecontract);
                }
            }

            if (i == (energiecontractList.size() - 1) && notEqual(currentEnergiecontract.getTotEnMet(), SINT_JUTTEMIS)) {
                currentEnergiecontract.setTotEnMet(SINT_JUTTEMIS);
                energiecontractRepository.save(currentEnergiecontract);
            }
            previousEnergiecontract = currentEnergiecontract;
        }
    }

    public List<Energiecontract> findAllInInPeriod(DateTimePeriod period) {
        long periodeVan = toMillisSinceEpoch(period.getStartDateTime());
        long periodeTotEnMet = toMillisSinceEpoch(period.getEndDateTime());
        return energiecontractRepository.findAllInInPeriod(periodeVan, periodeTotEnMet);
    }
}
