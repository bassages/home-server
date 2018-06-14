package nl.homeserver.klimaat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class KlimaatBuilder {

    private LocalDateTime datumTijd;
    private KlimaatSensor klimaatSensor;
    private BigDecimal temperatuur;
    private BigDecimal luchtvochtigheid;

    private KlimaatBuilder() {
        // Hide public constructor
    }

    public static KlimaatBuilder aKlimaat() {
        return new KlimaatBuilder();
    }

    public KlimaatBuilder withDatumtijd(final LocalDateTime datumTijd) {
        this.datumTijd = datumTijd;
        return this;
    }

    public KlimaatBuilder withKlimaatSensor(final KlimaatSensor klimaatSensor) {
        this.klimaatSensor = klimaatSensor;
        return this;
    }

    public KlimaatBuilder withTemperatuur(final BigDecimal temperatuur) {
        this.temperatuur = temperatuur;
        return this;
    }

    public KlimaatBuilder withLuchtvochtigheid(final BigDecimal luchtvochtigheid) {
        this.luchtvochtigheid = luchtvochtigheid;
        return this;
    }

    public Klimaat build() {
        final Klimaat klimaat = new Klimaat();
        klimaat.setDatumtijd(datumTijd);
        klimaat.setKlimaatSensor(klimaatSensor);
        klimaat.setLuchtvochtigheid(luchtvochtigheid);
        klimaat.setTemperatuur(temperatuur);
        return klimaat;
    }
}
