package nl.wiegman.home.api;

import nl.wiegman.home.model.Energiecontract;
import nl.wiegman.home.service.EnergiecontractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/energiecontract")
public class EnergiecontractController {

    private final EnergiecontractService energiecontractService;

    @Autowired
    public EnergiecontractController(EnergiecontractService energiecontractService) {
        this.energiecontractService = energiecontractService;
    }

    @GetMapping
    public List<Energiecontract> getAll() {
        return energiecontractService.getAll();
    }

    @GetMapping("/current")
    public Energiecontract current() {
        return energiecontractService.getCurrent();
    }

    @PostMapping
    public Energiecontract save(@RequestBody Energiecontract energiecontract) {
        return energiecontractService.save(energiecontract);
    }

    @DeleteMapping(path = "{id}")
    public void delete(@PathVariable("id") long id) {
        energiecontractService.delete(id);
    }
}
