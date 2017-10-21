package nl.wiegman.home.energie;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;

public class Verbruik {

    private static final int KOSTEN_SCALE = 3;

    private BigDecimal verbruik;
    private BigDecimal kosten;

    public void addVerbruik(BigDecimal verbruik) {
        if (this.verbruik == null) {
            this.verbruik = ZERO;
        }
        this.verbruik = this.verbruik.add(verbruik);
    }

    public void addKosten(BigDecimal kosten) {
        if (this.kosten == null) {
            this.kosten = ZERO;
        }
        this.kosten = this.kosten.add(kosten);
    }

    public BigDecimal getVerbruik() {
        return verbruik;
    }

    public void setVerbruik(BigDecimal verbruik) {
        this.verbruik = verbruik;
    }

    public BigDecimal getKosten() {
        if (kosten == null) {
            return null;
        }
        return kosten.setScale(KOSTEN_SCALE, HALF_UP);
    }

    public void setKosten(BigDecimal kosten) {
        this.kosten = kosten;
    }
}
