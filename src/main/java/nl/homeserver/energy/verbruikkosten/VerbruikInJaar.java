package nl.homeserver.energy.verbruikkosten;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record VerbruikInJaar(
        int jaar,
        @JsonUnwrapped
        VerbruikKostenOverzicht verbruikKostenOverzicht
) { }
