package nl.homeserver.klimaat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GemiddeldeKlimaatPerMaand {

    private LocalDate maand;
    private BigDecimal gemiddelde;

    public GemiddeldeKlimaatPerMaand(LocalDate maand, BigDecimal average) {
        this.maand = maand;
        this.gemiddelde = average;
    }

    public LocalDate getMaand() {
        return maand;
    }

    public BigDecimal getGemiddelde() {
        return gemiddelde;
    }
}
