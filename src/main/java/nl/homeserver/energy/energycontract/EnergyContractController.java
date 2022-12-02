package nl.homeserver.energy.energycontract;

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

        if (energyContractDto.id() == null) {
            energyContract = new EnergyContract();
        } else {
            energyContract = energyContractService.getById(energyContractDto.id());
        }
        energyContract.setValidFrom(energyContractDto.validFrom());
        energyContract.setElectricityPerKwhStandardTariff(energyContractDto.electricityPerKwhStandardTariff());
        energyContract.setElectricityPerKwhOffPeakTariff(energyContractDto.electricityPerKwhOffPeakTariff());
        energyContract.setGasPerCubicMeter(energyContractDto.gasPerCubicMeter());
        energyContract.setSupplierName(energyContractDto.supplierName());
        energyContract.setRemark(energyContractDto.remark());

        return energyContractService.save(energyContract);
    }

    @DeleteMapping(path = "{id}")
    public void delete(final @PathVariable("id") long id) {
        energyContractService.delete(id);
    }
}
