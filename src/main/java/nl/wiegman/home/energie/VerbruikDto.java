package nl.wiegman.home.energie;

import java.math.BigDecimal;

public class VerbruikDto {

    private BigDecimal stroomVerbruikDal;
    private BigDecimal stroomKostenDal;

    private BigDecimal stroomVerbruikNormaal;
    private BigDecimal stroomKostenNormaal;

    private BigDecimal gasVerbruik;
    private BigDecimal gasKosten;

    public BigDecimal getStroomVerbruikDal() {
        return stroomVerbruikDal;
    }

    public void setStroomVerbruikDal(BigDecimal stroomVerbruikDal) {
        this.stroomVerbruikDal = stroomVerbruikDal;
    }

    public BigDecimal getStroomKostenDal() {
        return stroomKostenDal;
    }

    public void setStroomKostenDal(BigDecimal stroomKostenDal) {
        this.stroomKostenDal = stroomKostenDal;
    }

    public BigDecimal getStroomVerbruikNormaal() {
        return stroomVerbruikNormaal;
    }

    public void setStroomVerbruikNormaal(BigDecimal stroomVerbruikNormaal) {
        this.stroomVerbruikNormaal = stroomVerbruikNormaal;
    }

    public BigDecimal getStroomKostenNormaal() {
        return stroomKostenNormaal;
    }

    public void setStroomKostenNormaal(BigDecimal stroomKostenNormaal) {
        this.stroomKostenNormaal = stroomKostenNormaal;
    }

    public BigDecimal getGasVerbruik() {
        return gasVerbruik;
    }

    public void setGasVerbruik(BigDecimal gasVerbruik) {
        this.gasVerbruik = gasVerbruik;
    }

    public BigDecimal getGasKosten() {
        return gasKosten;
    }

    public void setGasKosten(BigDecimal gasKosten) {
        this.gasKosten = gasKosten;
    }
}
