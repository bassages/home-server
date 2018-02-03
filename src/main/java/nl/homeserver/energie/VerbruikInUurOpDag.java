package nl.homeserver.energie;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Getter;

public class VerbruikInUurOpDag extends VerbruikKostenOverzicht {

    @Getter
    private int uur; // Range: 0 - 23

    @JsonUnwrapped
    private VerbruikKostenOverzicht verbruikKostenOverzicht;

    public VerbruikInUurOpDag(int uur, VerbruikKostenOverzicht verbruikKostenOverzicht) {
        this.uur = uur;
        this.verbruikKostenOverzicht = verbruikKostenOverzicht;
    }

    public VerbruikKostenOverzicht getVerbruikKostenOverzicht() {
        return verbruikKostenOverzicht;
    }
}
