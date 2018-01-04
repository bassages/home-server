package nl.wiegman.home.energie;

import static nl.wiegman.home.DatePeriod.aPeriodWithToDate;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.wiegman.home.DatePeriod;

@RestController
@RequestMapping("/api/meterstanden")
public class MeterstandController {

    private final MeterstandService meterstandService;

    @Autowired
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
        DatePeriod period = aPeriodWithToDate(from, to);
        return meterstandService.getPerDag(period);
    }
}
