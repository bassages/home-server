package nl.homeserver.energie;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class VerbruikInJaar {

    private int jaar;

    @JsonUnwrapped
    private VerbruikKostenOverzicht verbruikKostenOverzicht;

    public VerbruikInJaar(int jaar, VerbruikKostenOverzicht verbruikKostenOverzicht) {
        this.jaar = jaar;
        this.verbruikKostenOverzicht = verbruikKostenOverzicht;
    }

    public int getJaar() {
        return jaar;
    }

    public VerbruikKostenOverzicht getVerbruikKostenOverzicht() {
        return verbruikKostenOverzicht;
    }
}
