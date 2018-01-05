package nl.homeserver.energie;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.homeserver.DatePeriod;

@RestController
@RequestMapping("/api/energie")
public class EnergieController {

    private final VerbruikService verbruikService;

    @Autowired
    public EnergieController(VerbruikService verbruikService) {
        this.verbruikService = verbruikService;
    }

    @GetMapping(path = "gemiddelde-per-dag/{van}/{tot}")
    public VerbruikKostenOverzicht getGemiddeldeVerbruikPerDag(@PathVariable("van") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                                               @PathVariable("tot") @DateTimeFormat(iso = ISO.DATE) LocalDate to) {

        return verbruikService.getGemiddeldeVerbruikEnKostenInPeriode(DatePeriod.aPeriodWithToDate(from, to));
    }

    @GetMapping(path = "verbruik-per-jaar")
    public List<VerbruikInJaar> getVerbruikPerJaar() {
        return verbruikService.getVerbruikPerJaar();
    }

    @GetMapping(path = "verbruik-per-maand-in-jaar/{jaar}")
    public List<VerbruikInMaandInJaar> getVerbruikPerMaandInJaar(@PathVariable("jaar") int jaar) {
        return verbruikService.getVerbruikPerMaandInJaar(Year.of(jaar));
    }

    @GetMapping(path = "verbruik-per-dag/{van}/{tot}")
    public List<VerbruikKostenOpDag> getVerbruikPerDag(@PathVariable("van") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                                       @PathVariable("tot") @DateTimeFormat(iso = ISO.DATE) LocalDate to) {

        return verbruikService.getVerbruikPerDag(DatePeriod.aPeriodWithToDate(from, to));
    }

    @GetMapping(path = "verbruik-per-uur-op-dag/{dag}")
    public List<VerbruikInUurOpDag> getVerbruikPerUurOpDag(@PathVariable("dag") @DateTimeFormat(iso = ISO.DATE) LocalDate day) {
        return verbruikService.getVerbruikPerUurOpDag(day);
    }
}