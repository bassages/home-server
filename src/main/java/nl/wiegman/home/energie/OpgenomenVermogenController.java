package nl.wiegman.home.energie;

import static java.time.LocalDate.now;
import static nl.wiegman.home.DatePeriod.aPeriodWithToDate;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import nl.wiegman.home.DatePeriod;

@RestController
@RequestMapping("/api/opgenomen-vermogen")
public class OpgenomenVermogenController {

    private final OpgenomenVermogenService opgenomenVermogenService;
    private final Clock clock;

    @Autowired
    public OpgenomenVermogenController(OpgenomenVermogenService opgenomenVermogenService, Clock clock) {
        this.opgenomenVermogenService = opgenomenVermogenService;
        this.clock = clock;
    }

    @GetMapping("meest-recente")
    public OpgenomenVermogen getMostRecent() {
        return opgenomenVermogenService.getMostRecent();
    }

    @GetMapping(path = "historie/{from}/{to}")
    public List<OpgenomenVermogen> getOpgenomenVermogenHistory(@PathVariable("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                               @PathVariable("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                               @RequestParam("subPeriodLength") long subPeriodLength) {
        DatePeriod period = aPeriodWithToDate(from, to);

        if (to.isBefore(now(clock))) {
            return opgenomenVermogenService.getPotentiallyCachedHistory(period, subPeriodLength);
        } else {
            return opgenomenVermogenService.getHistory(period, subPeriodLength);
        }
    }
}