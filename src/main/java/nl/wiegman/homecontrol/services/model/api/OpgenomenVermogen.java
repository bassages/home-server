package nl.wiegman.homecontrol.services.model.api;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.context.ApplicationEvent;

/**
 * Bevat het opgenomen vermogen op een bepaald moment in tijd.
 */
public class OpgenomenVermogen {

    @ApiModelProperty(required = true, value = "Datum uitgedrukt in het aantal miliseconden sinds de zogenaamde 'Epoch (1 januari 1970, 0:00 uur)", example = "1318388699000")
    private long datumtijd;

    @ApiModelProperty(required = true)
    private int opgenomenVermogenInWatt;

    public long getDatumtijd() {
        return datumtijd;
    }

    public void setDatumtijd(long datumtijd) {
        this.datumtijd = datumtijd;
    }

    public int getOpgenomenVermogenInWatt() {
        return opgenomenVermogenInWatt;
    }

    public void setOpgenomenVermogenInWatt(int opgenomenVermogenInWatt) {
        this.opgenomenVermogenInWatt = opgenomenVermogenInWatt;
    }
}
