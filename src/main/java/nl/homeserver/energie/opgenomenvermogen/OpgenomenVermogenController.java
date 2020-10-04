package nl.homeserver.energie.opgenomenvermogen;

import static java.time.LocalDate.now;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import nl.homeserver.DatePeriod;
import nl.homeserver.config.Paths;

@RestController
@RequestMapping(Paths.API + "/opgenomen-vermogen")
@AllArgsConstructor
class OpgenomenVermogenController {

    private final OpgenomenVermogenService opgenomenVermogenService;
    private final Clock clock;

    @GetMapping("meest-recente")
    public OpgenomenVermogen getMostRecent() {
        return opgenomenVermogenService.getMostRecent();
    }

    @GetMapping("historie/{from}/{to}")
    public List<OpgenomenVermogen> getOpgenomenVermogenHistory(
            final @PathVariable("from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            final @PathVariable("to") @DateTimeFormat(iso = ISO.DATE) LocalDate to,
            final @RequestParam("subPeriodLength") long subPeriodLengthInSeconds) {

        final Duration subPeriodDuration = Duration.ofSeconds(subPeriodLengthInSeconds);
        final DatePeriod period = aPeriodWithToDate(from, to);

        if (to.isBefore(now(clock))) {
            return opgenomenVermogenService.getPotentiallyCachedHistory(period, subPeriodDuration);
        } else {
            return opgenomenVermogenService.getHistory(period, subPeriodDuration);
        }
    }
}