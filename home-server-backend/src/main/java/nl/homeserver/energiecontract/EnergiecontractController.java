package nl.homeserver.energiecontract;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/energiecontract")
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
    public Energiecontract save(final @RequestBody Energiecontract energiecontract) {
        if (energiecontract.getId() != null) {
            energiecontract.setValidTo(energiecontractService.getById(energiecontract.getId()).getValidTo());
        }
        return energiecontractService.save(energiecontract);
    }

    @DeleteMapping(path = "{id}")
    public void delete(final @PathVariable("id") long id) {
        energiecontractService.delete(id);
    }
}
