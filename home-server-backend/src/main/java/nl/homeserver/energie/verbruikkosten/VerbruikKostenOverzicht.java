package nl.homeserver.energie.verbruikkosten;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

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
        if (stroomKostenNormaal != null && stroomKostenDal != null) {
            return stroomKostenDal.add(stroomKostenNormaal);
        }
        return null;
    }
}
