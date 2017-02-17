package nl.wiegman.home.api;

import nl.wiegman.home.model.Meterstand;
import nl.wiegman.home.model.MeterstandOpDag;
import nl.wiegman.home.service.MeterstandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/meterstanden")
public class MeterstandServiceRest {

    @Autowired
    private MeterstandService meterstandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Meterstand save(@RequestBody Meterstand meterstand) {
        return meterstandService.save(meterstand);
    }

    @GetMapping("meest-recente")
    public Meterstand getMeestRecente() {
        return meterstandService.getMeestRecente();
    }

    @GetMapping("oudste")
    public Meterstand getOudste() {
        return meterstandService.getOudste();
    }

    @GetMapping("oudste-vandaag")
    public Meterstand getOudsteVandaag() {
        return meterstandService.getOudsteMeterstandOpDag(new Date());
    }

    @GetMapping("per-dag/{vanaf}/{totEnMet}")
    public List<MeterstandOpDag> perDag(@PathVariable("vanaf") long vanaf, @PathVariable("totEnMet") long totEnMet) {
        return meterstandService.perDag(vanaf, totEnMet);
    }

    @GetMapping("bestaat-op-datumtijd/{datumtijd}")
    public boolean bestaatOpDatumTijd(@PathVariable("datumtijd") long datumtijd) {
        return meterstandService.bestaatOpDatumTijd(datumtijd);
    }

}
