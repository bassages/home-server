package nl.homeserver.energie;

import java.time.LocalDate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeterstandOpDag {

    @Getter
    private final LocalDate dag;
    @Getter
    private final Meterstand meterstand;
}
