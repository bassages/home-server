package nl.wiegman.homecontrol.services.apimodel;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class AfvalInzameling {

    public enum AfvalType {
        GFT, REST, SALLCON, PLASTIC;
    }

    @ApiModelProperty(required = true, value = "Datum uitgedrukt in het aantal miliseconden sinds de zogenaamde 'Epoch (1 januari 1970, 0:00 uur)", example = "1318388699000")
    private long datum;

    @ApiModelProperty(required = true)
    private List<AfvalType> afvalTypes;

    public long getDatum() {
        return datum;
    }

    public void setDatum(long datum) {
        this.datum = datum;
    }

    public List<AfvalType> getAfvalTypes() {
        return afvalTypes;
    }

    public void setAfvalTypes(List<AfvalType> afvalTypes) {
        this.afvalTypes = afvalTypes;
    }
}
