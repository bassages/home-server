package nl.homeserver.energie;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;

public class VerbruikenEnKosten {

    private final Collection<VerbruikKosten> verbruikenEnKosten;

    public VerbruikenEnKosten(final Collection<VerbruikKosten> verbruikenEnKosten) {
        this.verbruikenEnKosten = verbruikenEnKosten;
    }

    private BigDecimal getTotaalVerbruik() {
        return verbruikenEnKosten.stream()
                                 .map(VerbruikKosten::getVerbruik)
                                 .filter(Objects::nonNull)
                                 .reduce(BigDecimal::add)
                                 .orElse(null);
    }

    private BigDecimal getTotaalKosten() {
        return verbruikenEnKosten.stream()
                                 .map(VerbruikKosten::getKosten)
                                 .filter(Objects::nonNull)
                                 .reduce(BigDecimal::add)
                                 .orElse(null);
    }

    public VerbruikKosten sumToSingle() {
        return  new VerbruikKosten(getTotaalVerbruik(), getTotaalKosten());
    }
}
