package nl.homeserver.energie.verbruikkosten;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public class VerbruikKostenOverzicht {

    @Getter
    @Setter
    private BigDecimal stroomVerbruikDal;

    @Getter
    @Setter
    private BigDecimal stroomKostenDal;

    @Getter
    @Setter
    private BigDecimal stroomVerbruikNormaal;

    @Getter
    @Setter
    private BigDecimal stroomKostenNormaal;

    @Getter
    @Setter
    private BigDecimal gasVerbruik;

    @Getter
    @Setter
    private BigDecimal gasKosten;

    public BigDecimal getTotaalStroomKosten() {
        return Arrays.stream(new BigDecimal[] { stroomKostenNormaal, stroomKostenDal })
                     .filter(Objects::nonNull)
                     .reduce(BigDecimal::add)
                     .orElse(null);
    }
}
