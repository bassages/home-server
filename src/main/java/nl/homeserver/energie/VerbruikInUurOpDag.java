package nl.homeserver.energie;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class VerbruikInUurOpDag extends VerbruikKostenOverzicht {

    private int uur; // Range: 0 - 23

    @JsonUnwrapped
    private VerbruikKostenOverzicht verbruikKostenOverzicht;

    public VerbruikInUurOpDag(int uur, VerbruikKostenOverzicht verbruikKostenOverzicht) {
        this.uur = uur;
        this.verbruikKostenOverzicht = verbruikKostenOverzicht;
    }

    public int getUur() {
        return uur;
    }

    public VerbruikKostenOverzicht getVerbruikKostenOverzicht() {
        return verbruikKostenOverzicht;
    }
}
