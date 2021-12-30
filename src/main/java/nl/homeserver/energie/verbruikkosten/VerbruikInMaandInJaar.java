package nl.homeserver.energie.verbruikkosten;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class VerbruikInMaandInJaar {

    private final int maand; // Range: 1 .. 12

    @JsonUnwrapped
    private final VerbruikKostenOverzicht verbruikKostenOverzicht;
}
