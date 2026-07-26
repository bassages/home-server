package nl.homeserver.energy.opgenomenvermogen;

import nl.homeserver.energy.StroomTariefIndicator;

import java.time.LocalDateTime;

import static nl.homeserver.energy.StroomTariefIndicator.NORMAAL;

@SuppressWarnings({ "FieldMayBeFinal", "WeakerAccess" })
public class OpgenomenVermogenBuilder {

    private LocalDateTime datumtijd = LocalDateTime.now();
    private StroomTariefIndicator stroomTariefIndicator = NORMAAL;
    private int activePowerTotalInWatts;
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

    public OpgenomenVermogen build() {
        final OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setId(id);
        opgenomenVermogen.setDatumtijd(datumtijd);
        opgenomenVermogen.setActivePowerTotalInWatts(activePowerTotalInWatts);
        opgenomenVermogen.setTariefIndicator(stroomTariefIndicator);
        return opgenomenVermogen;
    }
}
