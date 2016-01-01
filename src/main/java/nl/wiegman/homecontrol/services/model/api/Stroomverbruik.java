package nl.wiegman.homecontrol.services.model.api;

import java.math.BigDecimal;

public class Stroomverbruik {

    private int kWh;

    private BigDecimal euro;

    public int getkWh() {
        return kWh;
    }

    public void setkWh(int kWh) {
        this.kWh = kWh;
    }

    public BigDecimal getEuro() {
        return euro;
    }

    public void setEuro(BigDecimal euro) {
        this.euro = euro;
    }
}
