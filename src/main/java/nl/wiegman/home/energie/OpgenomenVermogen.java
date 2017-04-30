package nl.wiegman.home.energie;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bevat het opgenomen vermogen op een bepaald moment in tijd.
 */
public class OpgenomenVermogen {

    @JsonProperty
    private long dt;
    @JsonProperty
    private int watt;
    @JsonProperty
    private Short tarief;

    public OpgenomenVermogen(long datumtijd, int stroomOpgenomenVermogenInWatt, StroomTariefIndicator stroomTariefIndicator) {
        this.dt = datumtijd;
        this.watt = stroomOpgenomenVermogenInWatt;
        if (stroomTariefIndicator != null) {
            this.tarief = stroomTariefIndicator.getId();
        }
    }

    @JsonIgnore
    public void setDatumtijd(long datumtijd) {
        this.dt = datumtijd;
    }

    public void setOpgenomenVermogenInWatt(int opgenomenVermogenInWatt) {
        this.watt = opgenomenVermogenInWatt;
    }

    @JsonIgnore
    public int getOpgenomenVermogenInWatt() {
        return watt;
    }
}
