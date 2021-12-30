package nl.homeserver.energie.verbruikkosten;

import lombok.Getter;

import javax.annotation.Nullable;
import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

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
        return kosten == null ? null : kosten.setScale(KOSTEN_SCALE, HALF_UP);
    }

    public static VerbruikKosten empty() {
        return new VerbruikKosten(null, null);
    }
}
