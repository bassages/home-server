package nl.homeserver.energie;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;

public class Verbruiken {

    private final Collection<VerbruikKosten> verbruiken;

    public Verbruiken(final Collection<VerbruikKosten> verbruiken) {
        this.verbruiken = verbruiken;
    }

    private BigDecimal getTotaalVerbruik() {
        return verbruiken.stream()
                         .map(VerbruikKosten::getVerbruik)
                         .filter(Objects::nonNull)
                         .reduce(BigDecimal::add)
                         .orElse(null);
    }

    private BigDecimal getTotaalKosten() {
        return verbruiken.stream()
                .map(VerbruikKosten::getKosten)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(null);
    }

    public VerbruikKosten sumToSingle() {
        return  new VerbruikKosten(getTotaalVerbruik(), getTotaalKosten());
    }
}
