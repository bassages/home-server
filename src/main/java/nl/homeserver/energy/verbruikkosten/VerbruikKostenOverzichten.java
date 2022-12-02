package nl.homeserver.energy.verbruikkosten;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

class VerbruikKostenOverzichten {

    private final Collection<VerbruikKostenOverzicht> all;

    VerbruikKostenOverzichten(final Collection<VerbruikKostenOverzicht> all) {
        this.all = all;
    }

    VerbruikKostenOverzicht averageToSingle() {
        return VerbruikKostenOverzicht.builder()
            .stroomVerbruikDal(getAverage(all, VerbruikKostenOverzicht::getStroomVerbruikDal, 3))
            .stroomVerbruikNormaal(getAverage(all, VerbruikKostenOverzicht::getStroomVerbruikNormaal, 3))
            .gasVerbruik(getAverage(all, VerbruikKostenOverzicht::getGasVerbruik, 3))
            .stroomKostenDal(getAverage(all, VerbruikKostenOverzicht::getStroomKostenDal, 2))
            .stroomKostenNormaal(getAverage(all, VerbruikKostenOverzicht::getStroomKostenNormaal, 2))
            .gasKosten(getAverage(all, VerbruikKostenOverzicht::getGasKosten, 2))
            .build();
    }

    private BigDecimal getAverage(final Collection<VerbruikKostenOverzicht> verbruikKostenOverzichten,
                                  final Function<VerbruikKostenOverzicht, BigDecimal> attributeToAverageGetter,
                                  final int scale) {

        final BigDecimal sum = verbruikKostenOverzichten.stream()
                                                        .map(attributeToAverageGetter)
                                                        .filter(Objects::nonNull)
                                                        .reduce(ZERO, BigDecimal::add);

        return sum.divide(new BigDecimal(verbruikKostenOverzichten.size()), HALF_UP).setScale(scale, HALF_UP);
    }
}
