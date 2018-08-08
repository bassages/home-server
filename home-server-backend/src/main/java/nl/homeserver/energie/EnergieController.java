package nl.homeserver.energie;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static nl.homeserver.DatePeriod.aPeriodWithToDate;

@RestController
@RequestMapping("/api/energie")
public class EnergieController {

    private final VerbruikService verbruikService;

    public EnergieController(final VerbruikService verbruikService) {
        this.verbruikService = verbruikService;
    }

    @GetMapping(path = "gemiddelde-per-dag/{van}/{tot}")
    public VerbruikKostenOverzicht getGemiddeldeVerbruikPerDag(final @PathVariable("van") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                                               final @PathVariable("tot") @DateTimeFormat(iso = ISO.DATE) LocalDate to) {

        return verbruikService.getGemiddeldeVerbruikEnKostenInPeriode(aPeriodWithToDate(from, to));
    }

    @GetMapping(path = "verbruik-per-jaar")
    public List<VerbruikInJaar> getVerbruikPerJaar() {
        return verbruikService.getVerbruikPerJaar();
    }

    @GetMapping(path = "verbruik-per-maand-in-jaar/{jaar}")
    public List<VerbruikInMaandInJaar> getVerbruikPerMaandInJaar(final @PathVariable("jaar") int jaar) {
        return verbruikService.getVerbruikPerMaandInJaar(Year.of(jaar));
    }

    @GetMapping(path = "verbruik-per-dag/{van}/{tot}")
    public List<VerbruikKostenOpDag> getVerbruikPerDag(final @PathVariable("van") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                                       final @PathVariable("tot") @DateTimeFormat(iso = ISO.DATE) LocalDate to) {
        return verbruikService.getVerbruikPerDag(aPeriodWithToDate(from, to));
    }

    @GetMapping(path = "verbruik-per-uur-op-dag/{dag}")
    public List<VerbruikInUurOpDag> getVerbruikPerUurOpDag(final @PathVariable("dag") @DateTimeFormat(iso = ISO.DATE) LocalDate day) {
        return verbruikService.getVerbruikPerUurOpDag(day);
    }
}