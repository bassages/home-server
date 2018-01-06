package nl.homeserver.cache;

import static java.time.LocalDate.now;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.LongStream.rangeClosed;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import nl.homeserver.energie.EnergieController;
import nl.homeserver.energie.OpgenomenVermogenController;
import nl.homeserver.klimaat.KlimaatController;

@Component
public class WarmupCache implements ApplicationListener<ApplicationReadyEvent> {

    private final OpgenomenVermogenController opgenomenVermogenController;
    private final EnergieController energieController;
    private final KlimaatController klimaatController;
    private final Clock clock;

    @Value("${warmupCache.on-application-start}")
    private boolean warmupCacheOnApplicationStart;

    public WarmupCache(OpgenomenVermogenController opgenomenVermogenController, EnergieController energieController, KlimaatController klimaatController, Clock clock) {
        this.opgenomenVermogenController = opgenomenVermogenController;
        this.energieController = energieController;
        this.klimaatController = klimaatController;
        this.clock = clock;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (warmupCacheOnApplicationStart) {
            warmupEnergyCache();
            warmupClimateCache();
        }
    }

    private void warmupEnergyCache() {
        LocalDate today = now(clock);

        aPeriodWithToDate(today, today.plusDays(14)).getDays()
                                                    .forEach(day -> opgenomenVermogenController.getOpgenomenVermogenHistory(day,
                                                                                                                            day.plusDays(1),
                                                                                                                            MINUTES.toMillis(3)));

        aPeriodWithToDate(today, today.plusDays(14)).getDays()
                                                    .forEach(energieController::getVerbruikPerUurOpDag);

        rangeClosed(0, 24).forEach(monthToSubtract -> energieController.getVerbruikPerDag(today.minusMonths(monthToSubtract + 1).with(firstDayOfMonth()),
                                                                                          today.minusMonths(monthToSubtract).with(lastDayOfMonth())));

        rangeClosed(0, 2).forEach(yearsToSubTract -> energieController.getVerbruikPerMaandInJaar(today.minusYears(yearsToSubTract).getYear()));

        energieController.getVerbruikPerJaar();
    }

    private void warmupClimateCache() {
        LocalDate today = now(clock);

        aPeriodWithToDate(today, today.plusDays(14)).getDays()
                                                    .forEach(day -> klimaatController.findAllInPeriod(day, day.plusDays(1)));
    }
}
