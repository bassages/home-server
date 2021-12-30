package nl.homeserver.klimaat;

import lombok.Getter;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
class GemiddeldeKlimaatPerMaand {

    private final LocalDate maand;
    private final BigDecimal gemiddelde;

    GemiddeldeKlimaatPerMaand(final LocalDate maand, @Nullable final BigDecimal gemiddelde) {
        this.maand = maand;
        this.gemiddelde = gemiddelde;
    }
}
