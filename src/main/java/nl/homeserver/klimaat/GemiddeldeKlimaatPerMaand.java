package nl.homeserver.klimaat;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GemiddeldeKlimaatPerMaand {

    private final LocalDate maand;
    private final BigDecimal gemiddelde;
}
