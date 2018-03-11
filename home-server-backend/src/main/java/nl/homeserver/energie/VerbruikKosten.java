package nl.homeserver.energie;

import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VerbruikKosten {

    private static final int KOSTEN_SCALE = 3;

    @Getter
    private final BigDecimal verbruik;

    private final BigDecimal kosten;

    public static final VerbruikKosten UNKNOWN = new VerbruikKosten(null, null);

    public BigDecimal getKosten() {
        if (kosten == null) {
            return null;
        }
        return kosten.setScale(KOSTEN_SCALE, HALF_UP);
    }
}
