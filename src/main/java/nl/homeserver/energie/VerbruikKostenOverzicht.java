package nl.homeserver.energie;

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
}
