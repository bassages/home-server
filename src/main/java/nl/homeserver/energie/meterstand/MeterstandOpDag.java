package nl.homeserver.energie.meterstand;

import java.time.LocalDate;

import javax.annotation.Nullable;

import lombok.Getter;

public class MeterstandOpDag {

    @Getter
    private final LocalDate dag;
    @Getter
    private final Meterstand meterstand;

    public MeterstandOpDag(final LocalDate dag, @Nullable final Meterstand meterstand) {
        this.dag = dag;
        this.meterstand = meterstand;
    }
}
