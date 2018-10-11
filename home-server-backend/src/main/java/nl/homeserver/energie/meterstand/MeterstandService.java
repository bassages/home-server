package nl.homeserver.energie.meterstand;

import static java.time.LocalDate.now;
import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import nl.homeserver.DatePeriod;

@Service
public class MeterstandService {

    private static final String CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG = "meestRecenteMeterstandOpDag";

    static final String TOPIC = "/topic/meterstand";

    // Needed to make use of use caching annotations
    @Autowired
    private MeterstandService meterstandServiceProxyWithEnabledCaching;

    private final MeterstandRepository meterstandRepository;
    private final Clock clock;
    private final SimpMessagingTemplate messagingTemplate;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Meterstand> mostRecentlySavedMeterstand = Optional.empty();

    MeterstandService(final MeterstandRepository meterstandRepository,
                      final Clock clock,
                      final SimpMessagingTemplate messagingTemplate) {
        this.meterstandRepository = meterstandRepository;
        this.clock = clock;
        this.messagingTemplate = messagingTemplate;
    }

    public Meterstand save(final Meterstand meterstand) {
        final Meterstand savedMeterStand = meterstandRepository.save(meterstand);
        mostRecentlySavedMeterstand = Optional.of(savedMeterStand);
        messagingTemplate.convertAndSend(TOPIC, meterstand);
        return savedMeterStand;
    }

    public Meterstand getMostRecent() {
        return mostRecentlySavedMeterstand.orElseGet(meterstandRepository::getMostRecent);
    }

    public Meterstand getOldest() {
        return meterstandRepository.getOldest();
    }

    Meterstand getOldestOfToday() {
        final LocalDate today = now(clock);

        final LocalDateTime van = today.atStartOfDay();
        final LocalDateTime totEnMet = today.atStartOfDay().plusDays(1).minusNanos(1);

        final Meterstand oudsteStroomStandOpDag = meterstandRepository.getOldestInPeriod(van, totEnMet);

        if (oudsteStroomStandOpDag != null) {
            // Gas is registered once every hour, in the hour AFTER it actually is used. Compensate for that hour
            final Meterstand oudsteGasStandOpDag = meterstandRepository.getOldestInPeriod(van.plusHours(1), totEnMet.plusHours(1));

            if (oudsteGasStandOpDag != null) {
                oudsteStroomStandOpDag.setGas(oudsteGasStandOpDag.getGas());
            }
        }
        return oudsteStroomStandOpDag;
    }

    public List<MeterstandOpDag> getPerDag(final DatePeriod period) {
        return period.getDays().stream()
                               .map(this::getMeterstandOpDag)
                               .collect(toList());
    }

    private MeterstandOpDag getMeterstandOpDag(final LocalDate day) {
        return new MeterstandOpDag(day, getMeesteRecenteMeterstandOpDag(day));
    }

    @Nullable
    private Meterstand getMeesteRecenteMeterstandOpDag(final LocalDate day) {
        final LocalDate today = now(clock);
        if (day.isAfter(today)) {
            return null;
        } else if (day.isEqual(today)) {
            return getNonCachedMeestRecenteMeterstandOpDag(day);
        } else {
            return meterstandServiceProxyWithEnabledCaching.getPotentiallyCachedMeestRecenteMeterstandOpDag(day);
        }
    }

    @Cacheable(cacheNames = CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG)
    public Meterstand getPotentiallyCachedMeestRecenteMeterstandOpDag(final LocalDate day) {
        return getNonCachedMeestRecenteMeterstandOpDag(day);
    }

    private Meterstand getNonCachedMeestRecenteMeterstandOpDag(final LocalDate day) {
        final LocalDateTime van = day.atStartOfDay();
        final LocalDateTime totEnMet = day.atStartOfDay().plusDays(1).minusNanos(1);
        return meterstandRepository.getMostRecentInPeriod(van, totEnMet);
    }
}
