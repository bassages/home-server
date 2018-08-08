package nl.homeserver.energie;

import nl.homeserver.DatePeriod;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static nl.homeserver.DatePeriod.aPeriodWithToDate;

@RestController
@RequestMapping("/api/meterstanden")
public class MeterstandController {

    private final MeterstandService meterstandService;

    public MeterstandController(final MeterstandService meterstandService) {
        this.meterstandService = meterstandService;
    }

    @GetMapping("meest-recente")
    public Meterstand getMostRecent() {
        return meterstandService.getMostRecent();
    }

    @GetMapping("oudste-vandaag")
    public Meterstand getOldestOfToday() {
        return meterstandService.getOldestOfToday();
    }

    @GetMapping("per-dag/{vanaf}/{tot}")
    public List<MeterstandOpDag> perDag(final @PathVariable("vanaf") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                        final @PathVariable("tot") @DateTimeFormat(iso = ISO.DATE) LocalDate to) {
        final DatePeriod period = aPeriodWithToDate(from, to);
        return meterstandService.getPerDag(period);
    }
}
