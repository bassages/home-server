package nl.homeserver.energie;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class VerbruikKostenOpDag {

    @Getter
    private final LocalDate dag;

    @Getter
    @JsonUnwrapped
    private final VerbruikKostenOverzicht verbruikKostenOverzicht;
}
