package nl.homeserver.energie;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Getter;

public class VerbruikInMaandInJaar {

    @Getter
    private int maand; // Range: 1 .. 12

    @JsonUnwrapped
    private VerbruikKostenOverzicht verbruikKostenOverzicht;

    public VerbruikInMaandInJaar(int maand, VerbruikKostenOverzicht verbruikKostenOverzicht) {
        this.maand = maand;
        this.verbruikKostenOverzicht = verbruikKostenOverzicht;
    }

    public VerbruikKostenOverzicht getVerbruikKostenOverzicht() {
        return verbruikKostenOverzicht;
    }
}
