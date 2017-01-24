package nl.wiegman.home.api;

import nl.wiegman.home.model.VerbruikOpDag;
import nl.wiegman.home.model.VerbruikPerMaandInJaar;
import nl.wiegman.home.model.VerbruikPerUurOpDag;
import nl.wiegman.home.model.Energiesoort;
import nl.wiegman.home.service.VerbruikService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gas")
public class GasServiceRest {

    @Autowired
    VerbruikService verbruikService;

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