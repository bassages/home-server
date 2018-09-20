package nl.homeserver.energie;

import static nl.homeserver.energie.StroomTariefIndicator.NORMAAL;

import java.time.LocalDateTime;

class OpgenomenVermogenBuilder {

    private LocalDateTime datumtijd = LocalDateTime.now();
    private StroomTariefIndicator stroomTariefIndicator = NORMAAL;
    private int watt;
    private long id;

    static OpgenomenVermogenBuilder aOpgenomenVermogen() {
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

    public OpgenomenVermogenBuilder withWatt(final int watt) {
        this.watt = watt;
        return this;
    }

    public OpgenomenVermogen build() {
        final OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setId(id);
        opgenomenVermogen.setDatumtijd(datumtijd);
        opgenomenVermogen.setWatt(watt);
        opgenomenVermogen.setTariefIndicator(stroomTariefIndicator);
        return opgenomenVermogen;
    }
}
