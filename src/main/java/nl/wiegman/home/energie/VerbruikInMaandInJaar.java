package nl.wiegman.home.energie;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class VerbruikInMaandInJaar {

    private int maand; // Range: 1 .. 12

    @JsonUnwrapped
    private VerbruikKostenOverzicht verbruikKostenOverzicht;

    public VerbruikInMaandInJaar(int maand, VerbruikKostenOverzicht verbruikKostenOverzicht) {
        this.maand = maand;
        this.verbruikKostenOverzicht = verbruikKostenOverzicht;
    }

    public int getMaand() {
        return maand;
    }

    public VerbruikKostenOverzicht getVerbruikKostenOverzicht() {
        return verbruikKostenOverzicht;
    }
}
