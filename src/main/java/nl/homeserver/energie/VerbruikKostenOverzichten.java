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

    public VerbruikKostenOverzicht getAverages() {
        VerbruikKostenOverzicht verbruikKostenOverzicht = new VerbruikKostenOverzicht();
        verbruikKostenOverzicht.setStroomVerbruikDal(berekenGemiddelde(verbruikKostenOverzichten, VerbruikKostenOverzicht::getStroomVerbruikDal, 3));
        verbruikKostenOverzicht.setStroomVerbruikNormaal(berekenGemiddelde(verbruikKostenOverzichten, VerbruikKostenOverzicht::getStroomVerbruikNormaal, 3));
        verbruikKostenOverzicht.setGasVerbruik(berekenGemiddelde(verbruikKostenOverzichten, VerbruikKostenOverzicht::getGasVerbruik, 3));
        verbruikKostenOverzicht.setStroomKostenDal(berekenGemiddelde(verbruikKostenOverzichten, VerbruikKostenOverzicht::getStroomKostenDal, 2));
        verbruikKostenOverzicht.setStroomKostenNormaal(berekenGemiddelde(verbruikKostenOverzichten, VerbruikKostenOverzicht::getStroomKostenNormaal, 2));
        verbruikKostenOverzicht.setGasKosten(berekenGemiddelde(verbruikKostenOverzichten, VerbruikKostenOverzicht::getGasKosten, 2));
        return verbruikKostenOverzicht;
    }

    private BigDecimal berekenGemiddelde(final Collection<VerbruikKostenOverzicht> verbruikKostenOverzichtPerDag,
                                         final Function<VerbruikKostenOverzicht, BigDecimal> attributeToAverageGetter,
                                         final int scale) {

        BigDecimal sum = verbruikKostenOverzichtPerDag.stream()
                                                      .map(attributeToAverageGetter)
                                                      .filter(Objects::nonNull)
                                                      .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(new BigDecimal(verbruikKostenOverzichtPerDag.size()), ROUND_HALF_UP).setScale(scale, ROUND_HALF_UP);
    }

}
