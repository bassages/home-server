package nl.wiegman.home.energie;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/opgenomen-vermogen")
public class OpgenomenVermogenController {

    private final OpgenomenVermogenService opgenomenVermogenService;

    @Autowired
    public OpgenomenVermogenController(OpgenomenVermogenService opgenomenVermogenService) {
        this.opgenomenVermogenService = opgenomenVermogenService;
    }

    @GetMapping("meest-recente")
    public OpgenomenVermogen getMeestRecente() {
        return opgenomenVermogenService.getMeestRecente();
    }

    @GetMapping(path = "historie/{from}/{to}")
    public List<OpgenomenVermogen> getOpgenomenVermogenHistory(@PathVariable("from") long from, @PathVariable("to") long to, @RequestParam("subPeriodLength") long subPeriodLength) {
        if (to < System.currentTimeMillis()) {
            return opgenomenVermogenService.getPotentiallyCachedHistory(new Date(from), new Date(to), subPeriodLength);
        } else {
            return opgenomenVermogenService.getHistory(new Date(from), new Date(to), subPeriodLength);
        }
    }
}