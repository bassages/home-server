package nl.homeserver.energy.verbruikkosten;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

record VerbruikInUurOpDag(
    int uur, // Range: 0 - 23
    @JsonUnwrapped
    VerbruikKostenOverzicht verbruikKostenOverzicht
) { }
