package nl.wiegman.homecontrol.services.apimodel;

import java.util.List;

public class AfvalInzameling {

    public enum AfvalType {
        GFT,
        REST,
        SALLCON,
        PLASTIC
    }

    private String datum;
    private List<AfvalType> afvalTypes;

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public List<AfvalType> getAfvalTypes() {
        return afvalTypes;
    }

    public void setAfvalTypes(List<AfvalType> afvalTypes) {
        this.afvalTypes = afvalTypes;
    }
}
