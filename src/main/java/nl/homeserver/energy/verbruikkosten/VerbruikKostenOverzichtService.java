package nl.homeserver.energy.verbruikkosten;

import lombok.RequiredArgsConstructor;
import nl.homeserver.DateTimePeriod;
import nl.homeserver.energy.StroomTariefIndicator;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static nl.homeserver.energy.StroomTariefIndicator.DAL;
import static nl.homeserver.energy.StroomTariefIndicator.NORMAAL;

@RequiredArgsConstructor
@Service
public class VerbruikKostenOverzichtService {

    private final VerbruikKostenInPeriodService verbruikKostenInPeriodService;
    private final Clock clock;

    public VerbruikKostenOverzicht getVerbruikEnKostenOverzicht(final DateTimePeriod period,
                                                                final VerbruikProvider verbruikProvider) {

        final VerbruikKosten stroomVerbruikKostenDalTariefInPeriode = getStroomVerbruikInPeriode(verbruikProvider, period, DAL);
        final VerbruikKosten stroomVerbruikKostenNormaalTariefInPeriode = getStroomVerbruikInPeriode(verbruikProvider, period, NORMAAL);
        final VerbruikKosten gasVerbruikKostenInPeriode = getGasVerbruikInPeriode(verbruikProvider, period);

        return VerbruikKostenOverzicht.builder()
                .stroomVerbruikDal(stroomVerbruikKostenDalTariefInPeriode.getVerbruik())
                .stroomKostenDal(stroomVerbruikKostenDalTariefInPeriode.getKosten())
                .stroomVerbruikNormaal(stroomVerbruikKostenNormaalTariefInPeriode.getVerbruik())
                .stroomKostenNormaal(stroomVerbruikKostenNormaalTariefInPeriode.getKosten())
                .gasVerbruik(gasVerbruikKostenInPeriode.getVerbruik())
                .gasKosten(gasVerbruikKostenInPeriode.getKosten())
                .build();
    }

    private VerbruikKosten getGasVerbruikInPeriode(final VerbruikProvider verbruikProvider, final DateTimePeriod period) {
        final LocalDateTime now = now(clock);
        if (period.startsOnOrAfter(now)) {
            return VerbruikKosten.UNKNOWN;
        } else if (period.getEndDateTime().isBefore(now)) {
            return verbruikKostenInPeriodService.getPotentiallyCachedGasVerbruikInPeriode(verbruikProvider, period);
        } else {
            return verbruikKostenInPeriodService.getNotCachedGasVerbruikInPeriode(verbruikProvider, period);
        }
    }

    private VerbruikKosten getStroomVerbruikInPeriode(final VerbruikProvider verbruikProvider,
                                                      final DateTimePeriod period,
                                                      final StroomTariefIndicator stroomTariefIndicator) {
        final LocalDateTime now = now(clock);

        if (period.startsOnOrAfter(now)) {
            return VerbruikKosten.UNKNOWN;
        } else if (period.getEndDateTime().isBefore(now)) {
            return verbruikKostenInPeriodService.getPotentiallyCachedStroomVerbruikInPeriode(verbruikProvider, period, stroomTariefIndicator);
        } else {
            return verbruikKostenInPeriodService.getNotCachedStroomVerbruikInPeriode(verbruikProvider, period, stroomTariefIndicator);
        }
    }
}
