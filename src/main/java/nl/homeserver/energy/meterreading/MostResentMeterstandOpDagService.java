package nl.homeserver.energy.meterreading;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static nl.homeserver.CachingConfiguration.CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG;

@RequiredArgsConstructor
@Service
public class MostResentMeterstandOpDagService {
    private final MeterstandRepository meterstandRepository;

    @Cacheable(cacheNames = CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG)
    public Optional<Meterstand> getPotentiallyCachedMeestRecenteMeterstandOpDag(final LocalDate day) {
        return getNotCachedMeestRecenteMeterstandOpDag(day);
    }

    public Optional<Meterstand> getNotCachedMeestRecenteMeterstandOpDag(final LocalDate day) {
        final LocalDateTime van = day.atStartOfDay();
        final LocalDateTime totEnMet = day.atStartOfDay().plusDays(1).minusNanos(1);
        return meterstandRepository.findMostRecentInPeriod(van, totEnMet);
    }
}
