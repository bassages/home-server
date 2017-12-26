package nl.wiegman.home.energie;

import static nl.wiegman.home.DateTimePeriod.aPeriodWithEndDateTime;
import static nl.wiegman.home.DateTimeUtil.toLocalDateTime;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.wiegman.home.DateTimePeriod;

@RestController
@RequestMapping("/api/meterstanden")
public class MeterstandController {

    private final MeterstandService meterstandService;
    private final Clock clock;

    @Autowired
    public MeterstandController(MeterstandService meterstandService, Clock clock) {
        this.meterstandService = meterstandService;
        this.clock = clock;
    }

    @GetMapping("meest-recente")
    public Meterstand getMeestRecente() {
        return meterstandService.getMeestRecente();
    }

    @GetMapping("oudste-vandaag")
    public Meterstand getOudsteVandaag() {
        return meterstandService.getOudsteMeterstandOpDag(LocalDate.now(clock));
    }

    @GetMapping("per-dag/{vanaf}/{totEnMet}")
    public List<MeterstandOpDag> perDag(@PathVariable("vanaf") long startDateInMillisSinceEpoch, @PathVariable("totEnMet") long endDateInMillisSinceEpoch) {
        DateTimePeriod period = aPeriodWithEndDateTime(toLocalDateTime(startDateInMillisSinceEpoch), toLocalDateTime(endDateInMillisSinceEpoch));
        return meterstandService.perDag(period);
    }
}
