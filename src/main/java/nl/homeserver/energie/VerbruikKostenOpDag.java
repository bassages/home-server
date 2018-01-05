package nl.homeserver.energie;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class VerbruikKostenOpDag {

    private LocalDate dag;

    @JsonUnwrapped
    private VerbruikKostenOverzicht verbruikKostenOverzicht;

    public VerbruikKostenOpDag(LocalDate dag, VerbruikKostenOverzicht verbruikKostenOverzicht) {
        this.dag = dag;
        this.verbruikKostenOverzicht = verbruikKostenOverzicht;
    }

    public LocalDate getDag() {
        return dag;
    }

    public VerbruikKostenOverzicht getVerbruikKostenOverzicht() {
        return verbruikKostenOverzicht;
    }
}
