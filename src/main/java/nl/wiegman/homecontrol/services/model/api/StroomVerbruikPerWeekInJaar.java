package nl.wiegman.homecontrol.services.model.api;

public class StroomVerbruikPerWeekInJaar {

    private int week;

    private int kWh = 0;

    private double euro = 0;

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
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
