package nl.wiegman.home.energie;

public class MeterstandOpDag {

    private long datumtijd;

    private Meterstand meterstand;

    public MeterstandOpDag(long datumtijd, Meterstand meterstand) {
        this.datumtijd = datumtijd;
        this.meterstand = meterstand;
    }

    public long getDatumtijd() {
        return datumtijd;
    }

    public void setDatumtijd(long datumtijd) {
        this.datumtijd = datumtijd;
    }

    public Meterstand getMeterstand() {
        return meterstand;
    }

    public void setMeterstand(Meterstand meterstand) {
        this.meterstand = meterstand;
    }
}
