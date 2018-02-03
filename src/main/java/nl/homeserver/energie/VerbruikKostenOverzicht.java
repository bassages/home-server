package nl.homeserver.energie;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class VerbruikKostenOverzicht {

    private BigDecimal stroomVerbruikDal;
    private BigDecimal stroomKostenDal;

    private BigDecimal stroomVerbruikNormaal;
    private BigDecimal stroomKostenNormaal;

    private BigDecimal gasVerbruik;
    private BigDecimal gasKosten;
}
