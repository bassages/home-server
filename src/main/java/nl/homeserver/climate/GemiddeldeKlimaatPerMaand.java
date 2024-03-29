package nl.homeserver.climate;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;

record GemiddeldeKlimaatPerMaand(
        LocalDate maand,
        @Nullable
        BigDecimal gemiddelde
) { }
