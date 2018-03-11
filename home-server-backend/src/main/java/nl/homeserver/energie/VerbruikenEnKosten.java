package nl.homeserver.energie;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;

public class VerbruikenEnKosten {

    private final Collection<VerbruikKosten> all;

    public VerbruikenEnKosten(final Collection<VerbruikKosten> all) {
        this.all = all;
    }

    private BigDecimal getTotaalVerbruik() {
        return all.stream()
                  .map(VerbruikKosten::getVerbruik)
                  .filter(Objects::nonNull)
                  .reduce(BigDecimal::add)
                  .orElse(null);
    }

    private BigDecimal getTotaalKosten() {
        return all.stream()
                  .map(VerbruikKosten::getKosten)
                  .filter(Objects::nonNull)
                  .reduce(BigDecimal::add)
                  .orElse(null);
    }

    public VerbruikKosten sumToSingle() {
        return  new VerbruikKosten(getTotaalVerbruik(), getTotaalKosten());
    }
}
