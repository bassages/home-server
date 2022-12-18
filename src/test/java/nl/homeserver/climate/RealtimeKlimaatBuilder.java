package nl.homeserver.climate;

import java.time.LocalDateTime;

public class RealtimeKlimaatBuilder {

    private LocalDateTime datumTijd;

    private RealtimeKlimaatBuilder() {
        // Hide public constructor
    }

    public static RealtimeKlimaatBuilder aRealtimeKlimaat() {
        return new RealtimeKlimaatBuilder();
    }

    public RealtimeKlimaatBuilder withDatumtijd(final LocalDateTime datumTijd) {
        this.datumTijd = datumTijd;
        return this;
    }

    public RealtimeKlimaat build() {
        final RealtimeKlimaat klimaat = new RealtimeKlimaat();
        klimaat.setDatumtijd(datumTijd);
        return klimaat;
    }
}
