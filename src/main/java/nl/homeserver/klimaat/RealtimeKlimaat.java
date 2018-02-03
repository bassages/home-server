package nl.homeserver.klimaat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import nl.homeserver.Trend;

@Data
public class RealtimeKlimaat {
    private LocalDateTime datumtijd;
    private BigDecimal temperatuur;
    private BigDecimal luchtvochtigheid;
    private Trend temperatuurTrend;
    private Trend luchtvochtigheidTrend;
}
