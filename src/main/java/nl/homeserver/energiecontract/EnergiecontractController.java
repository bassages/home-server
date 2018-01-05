package nl.homeserver.energiecontract;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Energiecontract getCurrentlyValid() {
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
