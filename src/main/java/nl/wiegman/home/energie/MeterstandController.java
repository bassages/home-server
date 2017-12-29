package nl.wiegman.home.energie;

import static nl.wiegman.home.DatePeriod.aPeriodWithEndDate;
import static nl.wiegman.home.DateTimeUtil.toLocalDate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
    public Meterstand getMeestRecente() {
        return meterstandService.getMostRecent();
    }

    @GetMapping("oudste-vandaag")
    public Meterstand getOudsteVandaag() {
        return meterstandService.getOldestOfToday();
    }

    @GetMapping("per-dag/{vanaf}/{totEnMet}")
    public List<MeterstandOpDag> perDag(@PathVariable("vanaf") long startDateInMillisSinceEpoch, @PathVariable("totEnMet") long endDateInMillisSinceEpoch) {
        DatePeriod period = aPeriodWithEndDate(toLocalDate(startDateInMillisSinceEpoch), toLocalDate(endDateInMillisSinceEpoch));
        return meterstandService.perDag(period);
    }
}
