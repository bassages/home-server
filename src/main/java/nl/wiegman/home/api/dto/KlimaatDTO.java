package nl.wiegman.home.api.dto;

import java.math.BigDecimal;
import java.util.Date;

public class KlimaatDTO {

    private Date datumtijd;
    private BigDecimal temperatuur;
    private BigDecimal luchtvochtigheid;
    private String klimaatSensorCode;

    public Date getDatumtijd() {
        return datumtijd;
    }

    public void setDatumtijd(Date datumtijd) {
        this.datumtijd = datumtijd;
    }

    public BigDecimal getTemperatuur() {
        return temperatuur;
    }

    public void setTemperatuur(BigDecimal temperatuur) {
        this.temperatuur = temperatuur;
    }

    public BigDecimal getLuchtvochtigheid() {
        return luchtvochtigheid;
    }

    public void setLuchtvochtigheid(BigDecimal luchtvochtigheid) {
        this.luchtvochtigheid = luchtvochtigheid;
    }

    public String getKlimaatSensorCode() {
        return klimaatSensorCode;
    }

    public void setKlimaatSensorCode(String klimaatSensorCode) {
        this.klimaatSensorCode = klimaatSensorCode;
    }
}
