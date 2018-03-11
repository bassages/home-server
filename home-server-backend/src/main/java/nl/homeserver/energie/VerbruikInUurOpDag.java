package nl.homeserver.energie;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VerbruikInUurOpDag {

    @Getter
    private final int uur; // Range: 0 - 23

    @Getter
    @JsonUnwrapped
    private final VerbruikKostenOverzicht verbruikKostenOverzicht;
}
