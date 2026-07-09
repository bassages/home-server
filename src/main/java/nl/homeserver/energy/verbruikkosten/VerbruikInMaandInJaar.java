package nl.homeserver.energy.verbruikkosten;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record VerbruikInMaandInJaar(
        int maand, // Range: 1 .. 12
        @JsonUnwrapped
        VerbruikKostenOverzicht verbruikKostenOverzicht
) { }
