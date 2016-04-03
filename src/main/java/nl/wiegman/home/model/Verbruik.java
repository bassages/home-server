package nl.wiegman.home.model;

import java.math.BigDecimal;

public class Verbruik {

    private BigDecimal verbruik;

    private BigDecimal kosten;

    public BigDecimal getVerbruik() {
        return verbruik;
    }

    public void setVerbruik(BigDecimal kWh) {
        this.verbruik = kWh;
    }

    public BigDecimal getKosten() {
        return kosten;
    }

    public void setKosten(BigDecimal kosten) {
        this.kosten = kosten;
    }
}
