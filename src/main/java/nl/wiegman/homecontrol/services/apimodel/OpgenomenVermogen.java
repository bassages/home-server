package nl.wiegman.homecontrol.services.apimodel;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nl.wiegman.homecontrol.services.config.converters.LocalDateTimeJsonSerializer;

/**
 * Bevat opgenomen vermogen op een bepaald moment
 */
public class OpgenomenVermogen {

    private String datumtijd;
    private int opgenomenVermogenInWatt;

    public String getDatumtijd() {
        return datumtijd;
    }

//    @JsonSerialize(using = LocalDateTimeJsonSerializer.class)
    public void setDatumtijd(String datumtijd) {
        this.datumtijd = datumtijd;
    }

    public int getOpgenomenVermogenInWatt() {
        return opgenomenVermogenInWatt;
    }

    public void setOpgenomenVermogenInWatt(int opgenomenVermogenInWatt) {
        this.opgenomenVermogenInWatt = opgenomenVermogenInWatt;
    }
}
