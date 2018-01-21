package nl.homeserver.energie;

import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;

public class VerbruikKosten {

    private static final int KOSTEN_SCALE = 3;

    private BigDecimal verbruik;
    private BigDecimal kosten;

    public static final VerbruikKosten UNKNOWN = new VerbruikKosten(null, null);

    public VerbruikKosten(BigDecimal verbruik, BigDecimal kosten) {
        this.verbruik = verbruik;
        this.kosten = kosten;
    }

    public BigDecimal getVerbruik() {
        return verbruik;
    }

    public BigDecimal getKosten() {
        if (kosten == null) {
            return null;
        }
        return kosten.setScale(KOSTEN_SCALE, HALF_UP);
    }
}
