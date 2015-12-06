package nl.wiegman.homecontrol.services.model.api;

import io.swagger.annotations.ApiModelProperty;

public class StroomVerbruikPerMaandInJaar {

    private int maand;

    @ApiModelProperty(required = true)
    private int kWh = 0;

    @ApiModelProperty(required = true)
    private double euro = 0;

    public int getMaand() {
        return maand;
    }

    public void setMaand(int maand) {
        this.maand = maand;
    }

    public int getkWh() {
        return kWh;
    }

    public void setkWh(int kWh) {
        this.kWh = kWh;
    }

    public double getEuro() {
        return euro;
    }

    public void setEuro(double euro) {
        this.euro = euro;
    }
}
