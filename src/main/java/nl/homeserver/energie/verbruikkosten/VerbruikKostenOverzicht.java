package nl.homeserver.energie.verbruikkosten;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

@Getter
@Setter
@Builder
public class VerbruikKostenOverzicht {

    private BigDecimal stroomVerbruikDal;
    private BigDecimal stroomKostenDal;
    private BigDecimal stroomVerbruikNormaal;
    private BigDecimal stroomKostenNormaal;
    private BigDecimal gasVerbruik;
    private BigDecimal gasKosten;

    public BigDecimal getTotaalStroomKosten() {
        return Arrays.stream(new BigDecimal[] { stroomKostenNormaal, stroomKostenDal })
                     .filter(Objects::nonNull)
                     .reduce(BigDecimal::add)
                     .orElse(null);
    }
}
