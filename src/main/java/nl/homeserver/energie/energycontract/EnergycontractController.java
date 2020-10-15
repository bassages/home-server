package nl.homeserver.energie.energycontract;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import nl.homeserver.config.Paths;

@AllArgsConstructor
@RestController
@RequestMapping(Paths.API + "/energycontract")
class EnergycontractController {

    private final EnergycontractService energycontractService;

    @GetMapping
    public List<Energycontract> getAll() {
        return energycontractService.getAll();
    }

    @GetMapping("/current")
    public Energycontract getCurrentlyValid() {
        return energycontractService.getCurrent();
    }

    @PostMapping
    public Energycontract save(final @RequestBody EnergycontractDto energycontractDto) {
        final Energycontract energycontract;

        if (energycontractDto.getId() == null) {
            energycontract = new Energycontract();
        } else {
            energycontract = energycontractService.getById(energycontractDto.getId());
        }
        energycontract.setValidFrom(energycontractDto.getValidFrom());
        energycontract.setStroomPerKwhNormaalTarief(energycontractDto.getStroomPerKwhNormaalTarief());
        energycontract.setStroomPerKwhDalTarief(energycontractDto.getStroomPerKwhDalTarief());
        energycontract.setGasPerKuub(energycontractDto.getGasPerKuub());
        energycontract.setLeverancier(energycontractDto.getLeverancier());
        energycontract.setRemark(energycontractDto.getRemark());

        return energycontractService.save(energycontract);
    }

    @DeleteMapping(path = "{id}")
    public void delete(final @PathVariable("id") long id) {
        energycontractService.delete(id);
    }
}
