package nl.homeserver.climate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.annotation.Nullable;

class KlimaatBuilder {

    private LocalDateTime datumTijd;
    private KlimaatSensor klimaatSensor;
    private BigDecimal temperatuur;
    private BigDecimal luchtvochtigheid;

    private KlimaatBuilder() {
        // Hide public constructor
    }

    static KlimaatBuilder aKlimaat() {
        return new KlimaatBuilder();
    }

    KlimaatBuilder withDatumtijd(@Nullable final LocalDateTime datumTijd) {
        this.datumTijd = datumTijd;
        return this;
    }

    KlimaatBuilder withKlimaatSensor(@Nullable final KlimaatSensor klimaatSensor) {
        this.klimaatSensor = klimaatSensor;
        return this;
    }

    KlimaatBuilder withTemperatuur(@Nullable final BigDecimal temperatuur) {
        this.temperatuur = temperatuur;
        return this;
    }

    KlimaatBuilder withLuchtvochtigheid(@Nullable final BigDecimal luchtvochtigheid) {
        this.luchtvochtigheid = luchtvochtigheid;
        return this;
    }

    Klimaat build() {
        final Klimaat klimaat = new Klimaat();
        klimaat.setDatumtijd(datumTijd);
        klimaat.setKlimaatSensor(klimaatSensor);
        klimaat.setLuchtvochtigheid(luchtvochtigheid);
        klimaat.setTemperatuur(temperatuur);
        return klimaat;
    }
}
