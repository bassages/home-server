package nl.homeserver.energie;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.homeserver.DatePeriod;

@RestController
@RequestMapping("/api/meterstanden")
public class MeterstandController {

    private final MeterstandService meterstandService;

    public MeterstandController(MeterstandService meterstandService) {
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
    public List<MeterstandOpDag> perDag(@PathVariable("vanaf") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                        @PathVariable("tot") @DateTimeFormat(iso = ISO.DATE) LocalDate to) {
        DatePeriod period = DatePeriod.aPeriodWithToDate(from, to);
        return meterstandService.getPerDag(period);
    }
}
