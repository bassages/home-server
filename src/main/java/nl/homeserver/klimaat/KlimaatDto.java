package nl.homeserver.klimaat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

record KlimaatDto(
        long id,
        LocalDateTime datumtijd,
        BigDecimal temperatuur,
        BigDecimal luchtvochtigheid
) { }
