package nl.homeserver.energie;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VerbruikInMaandInJaar {

    @Getter
    private final int maand; // Range: 1 .. 12

    @Getter
    @JsonUnwrapped
    private VerbruikKostenOverzicht verbruikKostenOverzicht;
}
