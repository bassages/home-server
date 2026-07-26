package nl.homeserver.energy.opgenomenvermogen;

import nl.homeserver.energy.StroomTariefIndicator;

import java.time.LocalDateTime;

import static nl.homeserver.energy.StroomTariefIndicator.NORMAAL;

@SuppressWarnings({ "FieldMayBeFinal", "WeakerAccess" })
public class OpgenomenVermogenBuilder {

    private LocalDateTime datumtijd = LocalDateTime.now();
    private StroomTariefIndicator stroomTariefIndicator = NORMAAL;
    private int activePowerTotalInWatts;
    private int activePowerL1InWatts;
    private int activePowerL2InWatts;
    private int activePowerL3InWatts;
    private long id;

    public static OpgenomenVermogenBuilder aOpgenomenVermogen() {
        return new OpgenomenVermogenBuilder();
    }

    public OpgenomenVermogenBuilder withId(final long id) {
        this.id = id;
        return this;
    }

    public OpgenomenVermogenBuilder withDatumTijd(final LocalDateTime datumtijd) {
        this.datumtijd = datumtijd;
        return this;
    }

    public OpgenomenVermogenBuilder withActivePowerTotalInWatts(final int activePowerTotalInWatts) {
        this.activePowerTotalInWatts = activePowerTotalInWatts;
        return this;
    }

    public OpgenomenVermogenBuilder withActivePowerL1InWatts(final int activePowerL1InWatts) {
        this.activePowerL1InWatts = activePowerL1InWatts;
        return this;
    }

    public OpgenomenVermogenBuilder withActivePowerL2InWatts(final int activePowerL2InWatts) {
        this.activePowerL2InWatts = activePowerL2InWatts;
        return this;
    }

    public OpgenomenVermogenBuilder withActivePowerL3InWatts(final int activePowerL3InWatts) {
        this.activePowerL3InWatts = activePowerL3InWatts;
        return this;
    }

    public OpgenomenVermogen build() {
        final OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setId(id);
        opgenomenVermogen.setDatumtijd(datumtijd);
        opgenomenVermogen.setActivePowerTotalInWatts(activePowerTotalInWatts);
        opgenomenVermogen.setActivePowerL1InWatts(activePowerL1InWatts);
        opgenomenVermogen.setActivePowerL2InWatts(activePowerL2InWatts);
        opgenomenVermogen.setActivePowerL3InWatts(activePowerL3InWatts);
        opgenomenVermogen.setTariefIndicator(stroomTariefIndicator);
        return opgenomenVermogen;
    }
}
