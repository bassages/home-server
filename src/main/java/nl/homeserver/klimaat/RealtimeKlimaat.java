package nl.homeserver.klimaat;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
class RealtimeKlimaat {

    private LocalDateTime datumtijd;
    private BigDecimal temperatuur;
    private BigDecimal luchtvochtigheid;
    private Trend temperatuurTrend;
    private Trend luchtvochtigheidTrend;
    private String sensorCode;
}
