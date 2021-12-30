package nl.homeserver.energie.verbruikkosten;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class VerbruikInUurOpDag {

    private final int uur; // Range: 0 - 23

    @JsonUnwrapped
    private final VerbruikKostenOverzicht verbruikKostenOverzicht;
}
