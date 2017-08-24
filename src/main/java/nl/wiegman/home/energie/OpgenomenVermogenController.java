package nl.wiegman.home.energie;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
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
    public List<OpgenomenVermogen> getOpgenomenVermogenHistory(@PathVariable("from") long from, @PathVariable("to") long to,
            @RequestParam("subPeriodLength") long subPeriodLength) {
        if (to < System.currentTimeMillis()) {
            return opgenomenVermogenService.getPotentiallyCachedHistory(new Date(from), new Date(to), subPeriodLength);
        } else {
            return opgenomenVermogenService.getHistory(new Date(from), new Date(to), subPeriodLength);
        }
    }

    @GetMapping(path = "cleanup/{day}")
    public void cleanup(@PathVariable("day") long day) {
        opgenomenVermogenService.cleanup(new Date(day));
    }


    @GetMapping(path = "cleanup")
    public void cleanup() {
        try {
            Date twoDaysAgo = DateUtils.addDays(new Date(), -2);

            Date date = DateUtils.parseDate("26-02-2017", "dd-MM-yyyy");

            while (date.before(twoDaysAgo)) {
                opgenomenVermogenService.cleanup(date);
                date = DateUtils.addDays(date, 1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}