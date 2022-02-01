package nl.homeserver.energie.energycontract;

import lombok.RequiredArgsConstructor;
import nl.homeserver.config.Paths;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Paths.API + "/energycontract")
@RequiredArgsConstructor
class EnergyContractController {

    private final EnergyContractService energyContractService;

    @GetMapping
    public List<EnergyContract> getAll() {
        return energyContractService.getAll();
    }

    @GetMapping("/current")
    public EnergyContract getCurrentlyValid() {
        return energyContractService.getCurrent();
    }

    @PostMapping
    public EnergyContract save(final @RequestBody EnergyContractDto energyContractDto) {
        final EnergyContract energyContract;

        if (energyContractDto.getId() == null) {
            energyContract = new EnergyContract();
        } else {
            energyContract = energyContractService.getById(energyContractDto.getId());
        }
        energyContract.setValidFrom(energyContractDto.getValidFrom());
        energyContract.setStroomPerKwhNormaalTarief(energyContractDto.getStroomPerKwhNormaalTarief());
        energyContract.setStroomPerKwhDalTarief(energyContractDto.getStroomPerKwhDalTarief());
        energyContract.setGasPerKuub(energyContractDto.getGasPerKuub());
        energyContract.setLeverancier(energyContractDto.getLeverancier());
        energyContract.setRemark(energyContractDto.getRemark());

        return energyContractService.save(energyContract);
    }

    @DeleteMapping(path = "{id}")
    public void delete(final @PathVariable("id") long id) {
        energyContractService.delete(id);
    }
}
