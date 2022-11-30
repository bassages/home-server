package nl.homeserver.energie.verbruikkosten;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.time.LocalDate;

record VerbruikKostenOpDag(
    LocalDate dag,
    @JsonUnwrapped
    VerbruikKostenOverzicht verbruikKostenOverzicht
) { }
