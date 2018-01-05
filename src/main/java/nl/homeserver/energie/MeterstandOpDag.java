package nl.homeserver.energie;

import java.time.LocalDate;

public class MeterstandOpDag {

    private LocalDate dag;

    private Meterstand meterstand;

    public MeterstandOpDag(LocalDate dag, Meterstand meterstand) {
        this.meterstand = meterstand;
        this.dag = dag;
    }

    public LocalDate getDag() {
        return dag;
    }

    public Meterstand getMeterstand() {
        return meterstand;
    }
}
