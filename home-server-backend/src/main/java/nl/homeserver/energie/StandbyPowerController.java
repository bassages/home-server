package nl.homeserver.energie;

import static java.util.Collections.reverseOrder;
import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.homeserver.config.Paths;

@RestController
@RequestMapping(Paths.API + "/standby-power")
public class StandbyPowerController {

    private final StandbyPowerService standbyPowerService;
    private final Clock clock;

    public StandbyPowerController(final StandbyPowerService standbyPowerService,
                                  final Clock clock) {
        this.standbyPowerService = standbyPowerService;
        this.clock = clock;
    }

    @GetMapping(path = "{year}")
    public List<StandbyPowerInPeriod> getStandbyPower(@PathVariable("year") final int year) {
        final YearMonth currentYearMonth = YearMonth.now(clock);
        return IntStream.rangeClosed(1, 12)
                        .boxed().sorted(reverseOrder())
                        .map(month -> YearMonth.of(year, month))
                        .filter(yearMonth -> yearMonth.isBefore(currentYearMonth))
                        .map(standbyPowerService::getStandbyPower)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(toList());
    }

}
