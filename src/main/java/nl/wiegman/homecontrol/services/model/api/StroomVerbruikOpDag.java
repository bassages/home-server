package nl.wiegman.homecontrol.services.model.api;

import io.swagger.annotations.ApiModelProperty;

public class StroomVerbruikOpDag {

    @ApiModelProperty(required = true, value = "Datum uitgedrukt in het aantal miliseconden sinds de zogenaamde 'Epoch (1 januari 1970, 0:00 uur)", example = "1318388699000")
    private long dt;

    @ApiModelProperty(required = true)
    private int kWh;

    @ApiModelProperty(required = true)
    private double euro;

    public long getDt() {
        return dt;
    }

    public void setDt(long dt) {
        this.dt = dt;
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
