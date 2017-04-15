package nl.wiegman.home.api;

import nl.wiegman.home.model.Verbruik;
import nl.wiegman.home.api.dto.VerbruikOpDag;
import nl.wiegman.home.api.dto.VerbruikPerMaandInJaar;
import nl.wiegman.home.api.dto.VerbruikPerUurOpDag;
import nl.wiegman.home.model.Energiesoort;
import nl.wiegman.home.service.VerbruikService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

@RestController
@RequestMapping("/api/gas")
public class GasServiceRest {

    private final VerbruikService verbruikService;

    @Autowired
    public GasServiceRest(VerbruikService verbruikService) {
        this.verbruikService = verbruikService;
    }

    @GetMapping(path = "gemiddelde-per-dag-in-periode/{van}/{totEnMet}")
    public Verbruik getGemiddeldeVerbruikPerDagInPeriode(@PathVariable("van") long van, @PathVariable("totEnMet") long totEnMet) {
        List<VerbruikOpDag> verbruikPerDag = verbruikService.getVerbruikPerDag(Energiesoort.GAS, van, totEnMet);

        Verbruik verbruik = new Verbruik();
        verbruik.setVerbruik(berekenGemiddelde(verbruikPerDag, Verbruik::getVerbruik, 3));
        verbruik.setKosten(berekenGemiddelde(verbruikPerDag, Verbruik::getKosten, 2));
        return verbruik;
    }

    private BigDecimal berekenGemiddelde(List<VerbruikOpDag> verbruikPerDag, Function<VerbruikOpDag, BigDecimal> attributeToAverageGetter, int scale) {
        BigDecimal sumVerbruik = verbruikPerDag.stream().filter(attributeValue -> attributeToAverageGetter.apply(attributeValue) != null).map(attributeToAverageGetter).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sumVerbruik.divide(new BigDecimal(verbruikPerDag.size()), BigDecimal.ROUND_CEILING).setScale(scale, BigDecimal.ROUND_CEILING);
    }

    @GetMapping(path = "verbruik-per-maand-in-jaar/{jaar}")
    public List<VerbruikPerMaandInJaar> getVerbruikPerMaandInJaar(@PathVariable("jaar") int jaar) {
        return verbruikService.getVerbruikPerMaandInJaar(Energiesoort.GAS, jaar);
    }

    @GetMapping(path = "verbruik-per-dag/{van}/{totEnMet}")
    public List<VerbruikOpDag> getVerbruikPerDag(@PathVariable("van") long van, @PathVariable("totEnMet") long totEnMet) {
        return verbruikService.getVerbruikPerDag(Energiesoort.GAS, van, totEnMet);
    }

    @GetMapping(path = "verbruik-per-uur-op-dag/{dag}")
    public List<VerbruikPerUurOpDag> getVerbruikPerUurOpDag(@PathVariable("dag") long dag) {
        return verbruikService.getVerbruikPerUurOpDag(Energiesoort.GAS, dag);
    }
}