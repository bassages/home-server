package nl.homeserver.energie.verbruikkosten;

import lombok.RequiredArgsConstructor;
import nl.homeserver.DateTimePeriod;
import nl.homeserver.energie.StroomTariefIndicator;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static nl.homeserver.energie.StroomTariefIndicator.DAL;
import static nl.homeserver.energie.StroomTariefIndicator.NORMAAL;

@RequiredArgsConstructor
@Service
public class VerbruikKostenOverzichtService {

    public static final String CACHE_NAME_GAS_VERBRUIK_IN_PERIODE = "gasVerbruikInPeriode";
    public static final String CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE = "stroomVerbruikInPeriode";

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
