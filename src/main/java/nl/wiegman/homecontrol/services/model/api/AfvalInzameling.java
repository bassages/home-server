package nl.wiegman.homecontrol.services.model.api;

import java.util.List;

public class AfvalInzameling {

    public enum AfvalType {
        GFT, REST, SALLCON, PLASTIC;
    }

    private Long datum;

    private List<AfvalType> afvalTypes;

    public Long getDatum() {
        return datum;
    }

    public void setDatum(Long datum) {
        this.datum = datum;
    }

    public List<AfvalType> getAfvalTypes() {
        return afvalTypes;
    }

    public void setAfvalTypes(List<AfvalType> afvalTypes) {
        this.afvalTypes = afvalTypes;
    }
}
