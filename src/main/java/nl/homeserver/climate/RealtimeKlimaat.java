package nl.homeserver.climate;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
record RealtimeKlimaat(
    LocalDateTime datumtijd,
    BigDecimal temperatuur,
    BigDecimal luchtvochtigheid,
    Trend temperatuurTrend,
    Trend luchtvochtigheidTrend,
    String sensorCode) {

    static RealtimeKlimaatBuilder aRealtimeKlimaat() {
        return RealtimeKlimaat.builder();
    }
}
