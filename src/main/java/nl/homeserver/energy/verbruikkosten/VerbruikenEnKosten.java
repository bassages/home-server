package nl.homeserver.energy.verbruikkosten;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;

class VerbruikenEnKosten {

    private final Collection<VerbruikKosten> all;

    VerbruikenEnKosten(final Collection<VerbruikKosten> all) {
        this.all = all;
    }

    @Nullable
    private BigDecimal getTotaalVerbruik() {
        return all.stream()
                  .map(VerbruikKosten::getVerbruik)
                  .filter(Objects::nonNull)
                  .reduce(BigDecimal::add)
                  .orElse(null);
    }

    @Nullable
    private BigDecimal getTotaalKosten() {
        return all.stream()
                  .map(VerbruikKosten::getKosten)
                  .filter(Objects::nonNull)
                  .reduce(BigDecimal::add)
                  .orElse(null);
    }

    VerbruikKosten sumToSingle() {
        return  new VerbruikKosten(getTotaalVerbruik(), getTotaalKosten());
    }
}
