package nl.homeserver.energie.verbruikkosten;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

record VerbruikInJaar(
        int jaar,
        @JsonUnwrapped
        VerbruikKostenOverzicht verbruikKostenOverzicht
) { }
