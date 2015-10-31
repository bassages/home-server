package nl.wiegman.homecontrol.services.model.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * Bevat het opgenomen vermogen op een bepaald moment in tijd.
 */
public class OpgenomenVermogen {

    @JsonProperty
    @ApiModelProperty(required = true, value = "Datum uitgedrukt in het aantal miliseconden sinds de zogenaamde 'Epoch (1 januari 1970, 0:00 uur)", example = "1318388699000")
    private long dt;

    @JsonProperty
    @ApiModelProperty(required = true)
    private int watt;

    public OpgenomenVermogen(long datumtijd, int stroomOpgenomenVermogenInWatt) {
        this.dt = datumtijd;
        this.watt = stroomOpgenomenVermogenInWatt;
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
