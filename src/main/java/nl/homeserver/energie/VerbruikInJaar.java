package nl.homeserver.energie;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Getter;

public class VerbruikInJaar {

    @Getter
    private int jaar;

    @JsonUnwrapped
    private VerbruikKostenOverzicht verbruikKostenOverzicht;

    public VerbruikInJaar(int jaar, VerbruikKostenOverzicht verbruikKostenOverzicht) {
        this.jaar = jaar;
        this.verbruikKostenOverzicht = verbruikKostenOverzicht;
    }

    public VerbruikKostenOverzicht getVerbruikKostenOverzicht() {
        return verbruikKostenOverzicht;
    }
}
