package nl.homeserver.klimaat;

public final class KlimaatSensorBuilder {

    private String code;
    private String omschrijving;

    private KlimaatSensorBuilder() {
        // Hide public constructor
    }

    public static KlimaatSensorBuilder aKlimaatSensor() {
        return new KlimaatSensorBuilder();
    }

    public KlimaatSensorBuilder withCode(final String code) {
        this.code = code;
        return this;
    }

    public KlimaatSensorBuilder withOmschrijving(final String omschrijving) {
        this.omschrijving = omschrijving;
        return this;
    }

    public KlimaatSensor build() {
        final KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode(code);
        klimaatSensor.setOmschrijving(omschrijving);
        return klimaatSensor;
    }
}
