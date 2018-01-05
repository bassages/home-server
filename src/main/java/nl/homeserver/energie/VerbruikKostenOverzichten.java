package nl.homeserver.energie;

import static java.math.BigDecimal.ROUND_HALF_UP;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

public class VerbruikKostenOverzichten {

    private final Collection<VerbruikKostenOverzicht> verbruikKostenOverzichten;

    public VerbruikKostenOverzichten(final Collection<VerbruikKostenOverzicht> verbruikKostenOverzichten) {
        this.verbruikKostenOverzichten = verbruikKostenOverzichten;
    }

    public VerbruikKostenOverzicht averageToSingle() {
        VerbruikKostenOverzicht verbruikKostenOverzicht = new VerbruikKostenOverzicht();
        verbruikKostenOverzicht.setStroomVerbruikDal(getAverage(verbruikKostenOverzichten, VerbruikKostenOverzicht::getStroomVerbruikDal, 3));
        verbruikKostenOverzicht.setStroomVerbruikNormaal(getAverage(verbruikKostenOverzichten, VerbruikKostenOverzicht::getStroomVerbruikNormaal, 3));
        verbruikKostenOverzicht.setGasVerbruik(getAverage(verbruikKostenOverzichten, VerbruikKostenOverzicht::getGasVerbruik, 3));
        verbruikKostenOverzicht.setStroomKostenDal(getAverage(verbruikKostenOverzichten, VerbruikKostenOverzicht::getStroomKostenDal, 2));
        verbruikKostenOverzicht.setStroomKostenNormaal(getAverage(verbruikKostenOverzichten, VerbruikKostenOverzicht::getStroomKostenNormaal, 2));
        verbruikKostenOverzicht.setGasKosten(getAverage(verbruikKostenOverzichten, VerbruikKostenOverzicht::getGasKosten, 2));
        return verbruikKostenOverzicht;
    }

    private BigDecimal getAverage(final Collection<VerbruikKostenOverzicht> verbruikKostenOverzichten,
                                  final Function<VerbruikKostenOverzicht, BigDecimal> attributeToAverageGetter,
                                  final int scale) {

        BigDecimal sum = verbruikKostenOverzichten.stream()
                                                      .map(attributeToAverageGetter)
                                                      .filter(Objects::nonNull)
                                                      .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(new BigDecimal(verbruikKostenOverzichten.size()), ROUND_HALF_UP).setScale(scale, ROUND_HALF_UP);
    }

}
