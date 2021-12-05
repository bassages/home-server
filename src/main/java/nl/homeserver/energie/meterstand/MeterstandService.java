package nl.homeserver.energie.meterstand;

import lombok.RequiredArgsConstructor;
import nl.homeserver.DatePeriod;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.now;

@RequiredArgsConstructor
@Service
public class MeterstandService {

    static final String TOPIC = "/topic/meterstand";

    private final MostResentMeterstandOpDagService mostResentMeterstandOpDagService;
    private final MeterstandRepository meterstandRepository;
    private final Clock clock;
    private final SimpMessagingTemplate messagingTemplate;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Meterstand> mostRecentlySavedMeterstand = Optional.empty();

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

        final Meterstand oudsteStroomStandOpDag = meterstandRepository.findOldestInPeriod(van, totEnMet);

        if (oudsteStroomStandOpDag != null) {
            // Gas is registered once every hour, in the hour AFTER it actually is used. Compensate for that hour
            final Meterstand oudsteGasStandOpDag = meterstandRepository.findOldestInPeriod(van.plusHours(1), totEnMet.plusHours(1));

            if (oudsteGasStandOpDag != null) {
                oudsteStroomStandOpDag.setGas(oudsteGasStandOpDag.getGas());
            }
        }
        return oudsteStroomStandOpDag;
    }

    public List<MeterstandOpDag> getPerDag(final DatePeriod period) {
        return period.getDays()
                     .stream()
                     .map(day -> new MeterstandOpDag(day, getMeesteRecenteMeterstandOpDag(day).orElse(null)))
                     .toList();
    }

    public Optional<Meterstand> getMeesteRecenteMeterstandOpDag(final LocalDate day) {
        final LocalDate today = now(clock);

        if (day.isAfter(today)) {
            return Optional.empty();
        } else if (day.isEqual(today)) {
            return mostResentMeterstandOpDagService.getNotCachedMeestRecenteMeterstandOpDag(day);
        } else {
            return mostResentMeterstandOpDagService.getPotentiallyCachedMeestRecenteMeterstandOpDag(day);
        }
    }
}
