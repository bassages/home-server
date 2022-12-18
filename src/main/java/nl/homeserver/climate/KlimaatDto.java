package nl.homeserver.climate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

record KlimaatDto(
        long id,
        LocalDateTime datumtijd,
        BigDecimal temperatuur,
        BigDecimal luchtvochtigheid
) { }
