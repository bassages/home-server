package nl.wiegman.homecontrol.services.model.api;

import io.swagger.annotations.ApiModelProperty;

public class StroomMeterstand {

    @ApiModelProperty(required = true, value = "Datum uitgedrukt in het aantal miliseconden sinds de zogenaamde 'Epoch (1 januari 1970, 0:00 uur)", example = "1318388699000")
    private long datumtijd;

    @ApiModelProperty(required = true)
    private int verbruikInKwh;

    public long getDatumtijd() {
        return datumtijd;
    }

    public void setDatumtijd(long datumtijd) {
        this.datumtijd = datumtijd;
    }
}
