package nl.homeserver.energie.verbruikkosten;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

record VerbruikInMaandInJaar(
        int maand, // Range: 1 .. 12
        @JsonUnwrapped
        VerbruikKostenOverzicht verbruikKostenOverzicht
) { }
