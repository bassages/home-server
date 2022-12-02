package nl.homeserver.energy.standbypower;

import lombok.RequiredArgsConstructor;
import nl.homeserver.config.Paths;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.Collections.reverseOrder;

@RestController
@RequestMapping(Paths.API + "/standby-power")
@RequiredArgsConstructor
class StandbyPowerController {

    private final StandbyPowerService standbyPowerService;
    private final Clock clock;

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
                        .toList();
    }
}
