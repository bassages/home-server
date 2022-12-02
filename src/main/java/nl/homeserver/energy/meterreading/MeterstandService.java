package nl.homeserver.energy.meterreading;

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

    public Optional<Meterstand> getMostRecent() {
        return mostRecentlySavedMeterstand.or(meterstandRepository::getMostRecent);
    }

    public Optional<Meterstand> getOldest() {
        return meterstandRepository.getOldest();
    }

    Optional<Meterstand> findOldestOfToday() {
        final LocalDate today = now(clock);
        final LocalDateTime start = today.atStartOfDay();
        final LocalDateTime totEnMet = today.atStartOfDay().plusDays(1).minusNanos(1);

        final Optional<Meterstand> optionalOldestMeterReadingOnDay = meterstandRepository.findOldestInPeriod(start, totEnMet);

        optionalOldestMeterReadingOnDay.ifPresent(oldestMeterReadingOnDay -> {
                // Gas is registered once every hour, in the hour AFTER it actually is used. Compensate for that hour
            final Optional<Meterstand> optionalOldestGasMeterReadingOnDay = meterstandRepository.findOldestInPeriod(
                    start.plusHours(1), totEnMet.plusHours(1));

            optionalOldestGasMeterReadingOnDay.ifPresent(oldestGasMeterReadingOnDay ->
                    oldestMeterReadingOnDay.setGas(oldestGasMeterReadingOnDay.getGas()));
        });
        return optionalOldestMeterReadingOnDay;
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
