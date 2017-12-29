package nl.wiegman.home.klimaat;

import java.math.BigDecimal;
import java.util.Date;

public class GemiddeldeKlimaatPerMaand {

    private Date maand;
    private BigDecimal gemiddelde;

    public GemiddeldeKlimaatPerMaand(Date maand, BigDecimal average) {
        this.maand = maand;
        this.gemiddelde = average;
    }

    public Date getMaand() {
        return maand;
    }

    public BigDecimal getGemiddelde() {
        return gemiddelde;
    }
}
