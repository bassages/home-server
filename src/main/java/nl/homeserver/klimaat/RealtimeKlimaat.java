package nl.homeserver.klimaat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

class RealtimeKlimaat {

    @Getter
    @Setter
    private LocalDateTime datumtijd;

    @Getter
    @Setter
    private BigDecimal temperatuur;

    @Getter
    @Setter
    private BigDecimal luchtvochtigheid;

    @Getter
    @Setter
    private Trend temperatuurTrend;

    @Getter
    @Setter
    private Trend luchtvochtigheidTrend;

    @Getter
    @Setter
    private String sensorCode;
}
