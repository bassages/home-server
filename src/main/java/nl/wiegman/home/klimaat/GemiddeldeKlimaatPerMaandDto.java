package nl.wiegman.home.klimaat;

import java.math.BigDecimal;
import java.util.Date;

public class GemiddeldeKlimaatPerMaandDto {

    private Date maand;
    private BigDecimal gemiddelde;

    public Date getMaand() {
        return maand;
    }

    public void setMaand(Date maand) {
        this.maand = maand;
    }

    public BigDecimal getGemiddelde() {
        return gemiddelde;
    }

    public void setGemiddelde(BigDecimal gemiddelde) {
        this.gemiddelde = gemiddelde;
    }
}
