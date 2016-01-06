package nl.wiegman.homecontrol.services.model.api;

import java.math.BigDecimal;

public class Stroomverbruik {

    private Integer kWh;

    private BigDecimal euro;

    public Integer getkWh() {
        return kWh;
    }

    public void setkWh(Integer kWh) {
        this.kWh = kWh;
    }

    public BigDecimal getEuro() {
        return euro;
    }

    public void setEuro(BigDecimal euro) {
        this.euro = euro;
    }
}
