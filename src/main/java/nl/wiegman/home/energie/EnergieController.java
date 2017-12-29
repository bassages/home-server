package nl.wiegman.home.energie;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static nl.wiegman.home.DatePeriod.aPeriodWithToDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/energie")
public class EnergieController {

    private final VerbruikService verbruikService;

    @Autowired
    public EnergieController(VerbruikService verbruikService) {
        this.verbruikService = verbruikService;
    }

    @GetMapping(path = "gemiddelde-per-dag-in-periode/{van}/{tot}")
    public VerbruikDto getGemiddeldeVerbruikPerDagInPeriode(@PathVariable("van") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                                            @PathVariable("tot") @DateTimeFormat(iso = ISO.DATE) LocalDate to) {

        List<VerbruikOpDag> verbruikPerDag = verbruikService.getVerbruikPerDag(aPeriodWithToDate(from, to));

        VerbruikDto verbruik = new VerbruikDto();

        verbruik.setStroomVerbruikDal(berekenGemiddelde(verbruikPerDag, VerbruikDto::getStroomVerbruikDal, 3));
        verbruik.setStroomVerbruikNormaal(berekenGemiddelde(verbruikPerDag, VerbruikDto::getStroomVerbruikNormaal, 3));
        verbruik.setGasVerbruik(berekenGemiddelde(verbruikPerDag, VerbruikDto::getGasVerbruik, 3));

        verbruik.setStroomKostenDal(berekenGemiddelde(verbruikPerDag, VerbruikDto::getStroomKostenDal, 2));
        verbruik.setStroomKostenNormaal(berekenGemiddelde(verbruikPerDag, VerbruikDto::getStroomKostenNormaal, 2));
        verbruik.setGasKosten(berekenGemiddelde(verbruikPerDag, VerbruikDto::getGasKosten, 2));

        return verbruik;
    }

    private BigDecimal berekenGemiddelde(List<VerbruikOpDag> verbruikPerDag, Function<VerbruikOpDag, BigDecimal> attributeToAverageGetter, int scale) {
        BigDecimal sumVerbruik = verbruikPerDag.stream()
                .filter(attributeValue -> attributeToAverageGetter.apply(attributeValue) != null)
                .map(attributeToAverageGetter)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sumVerbruik.divide(new BigDecimal(verbruikPerDag.size()), ROUND_HALF_UP).setScale(scale, ROUND_HALF_UP);
    }

    @GetMapping(path = "verbruik-per-jaar")
    public List<VerbruikInJaar> getVerbruikPerJaar() {
        return verbruikService.getVerbruikPerJaar();
    }

    @GetMapping(path = "verbruik-per-maand-in-jaar/{jaar}")
    public List<VerbruikInMaandVanJaar> getVerbruikPerMaandInJaar(@PathVariable("jaar") int jaar) {
        return verbruikService.getVerbruikPerMaandInJaar(Year.of(jaar));
    }

    @GetMapping(path = "verbruik-per-dag/{van}/{tot}")
    public List<VerbruikOpDag> getVerbruikPerDag(@PathVariable("van") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                                 @PathVariable("tot") @DateTimeFormat(iso = ISO.DATE) LocalDate to) {
        return verbruikService.getVerbruikPerDag(aPeriodWithToDate(from, to));
    }

    @GetMapping(path = "verbruik-per-uur-op-dag/{dag}")
    public List<VerbruikInUurOpDag> getVerbruikPerUurOpDag(@PathVariable("dag") @DateTimeFormat(iso = ISO.DATE) LocalDate day) {
        return verbruikService.getVerbruikPerUurOpDag(day);
    }
}