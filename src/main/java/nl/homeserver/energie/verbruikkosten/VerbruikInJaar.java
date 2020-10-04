package nl.homeserver.energie.verbruikkosten;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class VerbruikInJaar {

    @Getter
    private final int jaar;

    @Getter
    @JsonUnwrapped
    private final VerbruikKostenOverzicht verbruikKostenOverzicht;
}
