package nl.homeserver.energie.meterstand;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class MostResentMeterstandOpDagService {

    private static final String CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG = "meestRecenteMeterstandOpDag";

    private final MeterstandRepository meterstandRepository;

    @Cacheable(cacheNames = CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG)
    public Meterstand getPotentiallyCachedMeestRecenteMeterstandOpDag(final LocalDate day) {
        return getNotCachedMeestRecenteMeterstandOpDag(day);
    }

    public Meterstand getNotCachedMeestRecenteMeterstandOpDag(LocalDate day) {
        final LocalDateTime van = day.atStartOfDay();
        final LocalDateTime totEnMet = day.atStartOfDay().plusDays(1).minusNanos(1);
        return meterstandRepository.getMostRecentInPeriod(van, totEnMet);
    }
}
