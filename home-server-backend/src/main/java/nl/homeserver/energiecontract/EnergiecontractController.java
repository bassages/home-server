package nl.homeserver.energiecontract;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.homeserver.config.Paths;

@RestController
@RequestMapping(Paths.API + "/energiecontract")
public class EnergiecontractController {

    private final EnergiecontractService energiecontractService;

    public EnergiecontractController(final EnergiecontractService energiecontractService) {
        this.energiecontractService = energiecontractService;
    }

    @GetMapping
    public List<Energiecontract> getAll() {
        return energiecontractService.getAll();
    }

    @GetMapping("/current")
    public Energiecontract getCurrentlyValid() {
        return energiecontractService.getCurrent();
    }

    @PostMapping
    public Energiecontract save(final @RequestBody EnergiecontractDto energiecontractDto) {
        final Energiecontract energiecontract;

        if (energiecontractDto.getId() == null) {
            energiecontract = new Energiecontract();
        } else {
            energiecontract = energiecontractService.getById(energiecontractDto.getId());
        }
        energiecontract.setValidFrom(energiecontractDto.getValidFrom());
        energiecontract.setStroomPerKwhNormaalTarief(energiecontractDto.getStroomPerKwhNormaalTarief());
        energiecontract.setStroomPerKwhDalTarief(energiecontractDto.getStroomPerKwhDalTarief());
        energiecontract.setGasPerKuub(energiecontractDto.getGasPerKuub());
        energiecontract.setLeverancier(energiecontractDto.getLeverancier());

        return energiecontractService.save(energiecontract);
    }

    @DeleteMapping(path = "{id}")
    public void delete(final @PathVariable("id") long id) {
        energiecontractService.delete(id);
    }
}
