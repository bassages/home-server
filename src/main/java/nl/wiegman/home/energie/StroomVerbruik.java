package nl.wiegman.home.energie;

import java.math.BigDecimal;

public class StroomVerbruik extends Verbruik {

    private BigDecimal verbruikNormaalTarief;
    private BigDecimal verbruikDalTarief;

    private BigDecimal kostenNormaalTarief;
    private BigDecimal kostenDalTarief;

    public BigDecimal getVerbruikNormaalTarief() {
        return verbruikNormaalTarief;
    }

    public void setVerbruikNormaalTarief(BigDecimal verbruikNormaalTarief) {
        this.verbruikNormaalTarief = verbruikNormaalTarief;
    }

    public BigDecimal getVerbruikDalTarief() {
        return verbruikDalTarief;
    }

    public void setVerbruikDalTarief(BigDecimal verbruikDalTarief) {
        this.verbruikDalTarief = verbruikDalTarief;
    }

    public BigDecimal getKostenNormaalTarief() {
        return kostenNormaalTarief;
    }

    public void setKostenNormaalTarief(BigDecimal kostenNormaalTarief) {
        this.kostenNormaalTarief = kostenNormaalTarief;
    }

    public BigDecimal getKostenDalTarief() {
        return kostenDalTarief;
    }

    public void setKostenDalTarief(BigDecimal kostenDalTarief) {
        this.kostenDalTarief = kostenDalTarief;
    }
}
