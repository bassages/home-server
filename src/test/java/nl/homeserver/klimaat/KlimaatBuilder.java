package nl.homeserver.klimaat;

import java.time.LocalDateTime;

public class KlimaatBuilder {

    private LocalDateTime datumTijd;
    private KlimaatSensor klimaatSensor;

    private KlimaatBuilder() {
        // Hide public constructor
    }

    public static KlimaatBuilder aKlimaat() {
        return new KlimaatBuilder();
    }

    public KlimaatBuilder withDatumtijd(LocalDateTime datumTijd) {
        this.datumTijd = datumTijd;
        return this;
    }

    public KlimaatBuilder withKlimaatSensor(KlimaatSensor klimaatSensor) {
        this.klimaatSensor = klimaatSensor;
        return this;
    }

    public Klimaat build() {
        Klimaat klimaat = new Klimaat();
        klimaat.setDatumtijd(datumTijd);
        klimaat.setKlimaatSensor(klimaatSensor);
        return klimaat;
    }
}
