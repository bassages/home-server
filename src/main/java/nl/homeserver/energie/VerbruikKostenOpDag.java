package nl.homeserver.energie;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Getter;

public class VerbruikKostenOpDag {

    @Getter
    private LocalDate dag;

    @JsonUnwrapped
    private VerbruikKostenOverzicht verbruikKostenOverzicht;

    public VerbruikKostenOpDag(LocalDate dag, VerbruikKostenOverzicht verbruikKostenOverzicht) {
        this.dag = dag;
        this.verbruikKostenOverzicht = verbruikKostenOverzicht;
    }

    public VerbruikKostenOverzicht getVerbruikKostenOverzicht() {
        return verbruikKostenOverzicht;
    }
}
