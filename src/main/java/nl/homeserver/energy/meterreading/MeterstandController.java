package nl.homeserver.energy.meterreading;

import lombok.RequiredArgsConstructor;
import nl.homeserver.DatePeriod;
import nl.homeserver.config.Paths;
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
@RequestMapping(Paths.API + "/meterstanden")
@RequiredArgsConstructor
class MeterstandController {

    private final MeterstandService meterstandService;

    @GetMapping("meest-recente")
    public Meterstand getMostRecent() {
        return meterstandService.getMostRecent().orElse(null);
    }

    @GetMapping("oudste-vandaag")
    public Meterstand getOldestOfToday() {
        return meterstandService.findOldestOfToday().orElse(null);
    }

    @GetMapping("per-dag/{vanaf}/{tot}")
    public List<MeterstandOpDag> perDag(final @PathVariable("vanaf") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                 final @PathVariable("tot") @DateTimeFormat(iso = ISO.DATE) LocalDate to) {
        final DatePeriod period = aPeriodWithToDate(from, to);
        return meterstandService.getPerDag(period);
    }
}
