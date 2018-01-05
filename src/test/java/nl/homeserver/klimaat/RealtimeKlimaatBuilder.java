package nl.homeserver.klimaat;

import java.time.LocalDateTime;

public class RealtimeKlimaatBuilder {

    private LocalDateTime datumTijd;

    private RealtimeKlimaatBuilder() {
        // Hide public constructor
    }

    public static RealtimeKlimaatBuilder aRealtimeKlimaat() {
        return new RealtimeKlimaatBuilder();
    }

    public RealtimeKlimaatBuilder withDatumtijd(LocalDateTime datumTijd) {
        this.datumTijd = datumTijd;
        return this;
    }

    public RealtimeKlimaat build() {
        RealtimeKlimaat klimaat = new RealtimeKlimaat();
        klimaat.setDatumtijd(datumTijd);
        return klimaat;
    }
}
