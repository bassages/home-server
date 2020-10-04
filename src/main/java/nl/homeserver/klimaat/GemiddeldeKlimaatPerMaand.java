package nl.homeserver.klimaat;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.annotation.Nullable;

import lombok.Getter;

class GemiddeldeKlimaatPerMaand {

    @Getter
    private final LocalDate maand;
    @Getter
    private final BigDecimal gemiddelde;

    GemiddeldeKlimaatPerMaand(final LocalDate maand, @Nullable final BigDecimal gemiddelde) {
        this.maand = maand;
        this.gemiddelde = gemiddelde;
    }
}
