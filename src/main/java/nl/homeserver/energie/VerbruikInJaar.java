package nl.homeserver.energie;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VerbruikInJaar {

    @Getter
    private final int jaar;

    @JsonUnwrapped
    private final VerbruikKostenOverzicht verbruikKostenOverzicht;
}
