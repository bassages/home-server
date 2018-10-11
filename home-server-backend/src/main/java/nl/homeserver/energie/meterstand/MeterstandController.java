package nl.homeserver.energie.meterstand;

import static nl.homeserver.DatePeriod.aPeriodWithToDate;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import nl.homeserver.DatePeriod;
import nl.homeserver.config.Paths;

@RestController
@RequestMapping(Paths.API + "/meterstanden")
@AllArgsConstructor
class MeterstandController {

    private final MeterstandService meterstandService;

    @GetMapping("meest-recente")
    Meterstand getMostRecent() {
        return meterstandService.getMostRecent();
    }

    @GetMapping("oudste-vandaag")
    Meterstand getOldestOfToday() {
        return meterstandService.getOldestOfToday();
    }

    @GetMapping("per-dag/{vanaf}/{tot}")
    List<MeterstandOpDag> perDag(final @PathVariable("vanaf") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                 final @PathVariable("tot") @DateTimeFormat(iso = ISO.DATE) LocalDate to) {
        final DatePeriod period = aPeriodWithToDate(from, to);
        return meterstandService.getPerDag(period);
    }
}
