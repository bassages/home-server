package nl.wiegman.homecontrol.services.model.api;

public class StroomVerbruikOpDag {

    private long dt;

    private int kWh;

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
