package nl.homeserver.energie;

import nl.homeserver.DatePeriod;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.now;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;

@RestController
@RequestMapping("/api/opgenomen-vermogen")
public class OpgenomenVermogenController {

    private final OpgenomenVermogenService opgenomenVermogenService;
    private final Clock clock;

    public OpgenomenVermogenController(final OpgenomenVermogenService opgenomenVermogenService, final Clock clock) {
        this.opgenomenVermogenService = opgenomenVermogenService;
        this.clock = clock;
    }

    @GetMapping("meest-recente")
    public OpgenomenVermogen getMostRecent() {
        return opgenomenVermogenService.getMostRecent();
    }

    @GetMapping(path = "historie/{from}/{to}")
    public List<OpgenomenVermogen> getOpgenomenVermogenHistory(final @PathVariable("from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
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