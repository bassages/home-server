package nl.wiegman.home.api;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.wiegman.home.model.Meterstand;
import nl.wiegman.home.api.dto.MeterstandOpDag;
import nl.wiegman.home.service.MeterstandService;

@RestController
@RequestMapping("/api/meterstanden")
public class MeterstandenServiceRest {

    private final MeterstandService meterstandService;

    @Autowired
    public MeterstandenServiceRest(MeterstandService meterstandService) {
        this.meterstandService = meterstandService;
    }

    @GetMapping("meest-recente")
    public Meterstand getMeestRecente() {
        return meterstandService.getMeestRecente();
    }

    @GetMapping("oudste-vandaag")
    public Meterstand getOudsteVandaag() {
        return meterstandService.getOudsteMeterstandOpDag(new Date());
    }

    @GetMapping("per-dag/{vanaf}/{totEnMet}")
    public List<MeterstandOpDag> perDag(@PathVariable("vanaf") long vanaf, @PathVariable("totEnMet") long totEnMet) {
        return meterstandService.perDag(vanaf, totEnMet);
    }
}
