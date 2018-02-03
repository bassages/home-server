package nl.homeserver.energie;

import java.time.LocalDate;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MeterstandOpDag {

    private final LocalDate dag;
    private final Meterstand meterstand;
}
