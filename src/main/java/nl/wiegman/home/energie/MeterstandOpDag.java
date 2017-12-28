package nl.wiegman.home.energie;

import static nl.wiegman.home.DateTimeUtil.toMillisSinceEpochAtStartOfDay;

import java.time.LocalDate;

public class MeterstandOpDag {

    private long datumtijd;

    private Meterstand meterstand;

    public MeterstandOpDag(LocalDate dag, Meterstand meterstand) {
        this.meterstand = meterstand;
        this.datumtijd = toMillisSinceEpochAtStartOfDay(dag);
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
