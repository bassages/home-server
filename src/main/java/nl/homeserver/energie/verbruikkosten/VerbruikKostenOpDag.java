package nl.homeserver.energie.verbruikkosten;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
class VerbruikKostenOpDag {

    private final LocalDate dag;

    @JsonUnwrapped
    private final VerbruikKostenOverzicht verbruikKostenOverzicht;
}
