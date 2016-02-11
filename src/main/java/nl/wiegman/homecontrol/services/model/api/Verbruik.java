package nl.wiegman.homecontrol.services.model.api;

import java.math.BigDecimal;

public class Verbruik {

    private BigDecimal verbruik;

    private BigDecimal euro;

    public BigDecimal getVerbruik() {
        return verbruik;
    }

    public void setVerbruik(BigDecimal kWh) {
        this.verbruik = kWh;
    }

    public BigDecimal getEuro() {
        return euro;
    }

    public void setEuro(BigDecimal euro) {
        this.euro = euro;
    }
}
