package nl.homeserver.klimaat;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GemiddeldeKlimaatPerMaand {

    @Getter
    private final LocalDate maand;
    @Getter
    private final BigDecimal gemiddelde;
}
