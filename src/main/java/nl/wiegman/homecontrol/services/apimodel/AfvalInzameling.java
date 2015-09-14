package nl.wiegman.homecontrol.services.apimodel;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 */
public class AfvalInzameling {

    private String datum;
    private String omschrijving;

    @ApiModelProperty(required = true)
    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    @ApiModelProperty(required = true)
    public String getOmschrijving() {
        return omschrijving;
    }

    public void setOmschrijving(String omschrijving) {
        this.omschrijving = omschrijving;
    }
}
