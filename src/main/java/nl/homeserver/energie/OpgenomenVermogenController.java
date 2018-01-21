package nl.homeserver.energie;

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

import nl.homeserver.DatePeriod;

@RestController
@RequestMapping("/api/opgenomen-vermogen")
public class OpgenomenVermogenController {

    private final OpgenomenVermogenService opgenomenVermogenService;
    private final Clock clock;

    public OpgenomenVermogenController(OpgenomenVermogenService opgenomenVermogenService, Clock clock) {
        this.opgenomenVermogenService = opgenomenVermogenService;
        this.clock = clock;
    }

    @GetMapping("meest-recente")
    public OpgenomenVermogen getMostRecent() {
        return opgenomenVermogenService.getMostRecent();
    }

    @GetMapping(path = "historie/{from}/{to}")
    public List<OpgenomenVermogen> getOpgenomenVermogenHistory(@PathVariable("from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                                               @PathVariable("to") @DateTimeFormat(iso = ISO.DATE) LocalDate to,
                                                               @RequestParam("subPeriodLength") long subPeriodLengthInSeconds) {

        Duration subPeriodDuration = Duration.ofSeconds(subPeriodLengthInSeconds);
        DatePeriod period = aPeriodWithToDate(from, to);

        if (to.isBefore(now(clock))) {
            return opgenomenVermogenService.getPotentiallyCachedHistory(period, subPeriodDuration);
        } else {
            return opgenomenVermogenService.getHistory(period, subPeriodDuration);
        }
    }
}