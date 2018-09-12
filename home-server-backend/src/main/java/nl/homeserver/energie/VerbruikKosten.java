package nl.homeserver.energie;

import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;

import javax.annotation.Nullable;

import lombok.Getter;

class VerbruikKosten {

    private static final int KOSTEN_SCALE = 3;

    @Getter
    private final BigDecimal verbruik;

    private final BigDecimal kosten;

    static final VerbruikKosten UNKNOWN = new VerbruikKosten(null, null);

    VerbruikKosten(@Nullable final BigDecimal verbruik, @Nullable final BigDecimal kosten) {
        this.verbruik = verbruik;
        this.kosten = kosten;
    }

    BigDecimal getKosten() {
        if (kosten == null) {
            return null;
        }
        return kosten.setScale(KOSTEN_SCALE, HALF_UP);
    }
}
