package nl.homeserver.energie.verbruikkosten;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class VerbruikInJaar {

    private final int jaar;

    @JsonUnwrapped
    private final VerbruikKostenOverzicht verbruikKostenOverzicht;
}
