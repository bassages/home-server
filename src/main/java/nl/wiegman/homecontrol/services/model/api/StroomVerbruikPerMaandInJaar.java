package nl.wiegman.homecontrol.services.model.api;

public class StroomVerbruikPerMaandInJaar {

    private int maand;

    private int kWh = 0;

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
