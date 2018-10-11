package nl.homeserver.energie.standbypower;

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

import lombok.AllArgsConstructor;
import nl.homeserver.config.Paths;

@RestController
@RequestMapping(Paths.API + "/standby-power")
@AllArgsConstructor
class StandbyPowerController {

    private final StandbyPowerService standbyPowerService;
    private final Clock clock;

    @GetMapping(path = "{year}")
    List<StandbyPowerInPeriod> getStandbyPower(@PathVariable("year") final int year) {
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
