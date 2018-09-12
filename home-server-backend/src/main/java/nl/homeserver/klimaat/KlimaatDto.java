package nl.homeserver.klimaat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KlimaatDto {

    private long id;
    private LocalDateTime datumtijd;
    private BigDecimal temperatuur;
    private BigDecimal luchtvochtigheid;
}
