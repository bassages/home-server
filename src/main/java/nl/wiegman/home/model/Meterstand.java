package nl.wiegman.home.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
public class Meterstand {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    private long datumtijd;

    @Column(nullable = false)
    private int stroomOpgenomenVermogenInWatt;

    @Column(nullable = false, precision = 8, scale = 3)
    private BigDecimal stroomTarief1;

    @Column(nullable = false, precision = 8, scale = 3)
    private BigDecimal stroomTarief2;

    @Column(nullable = false, precision = 8, scale = 3)
    private BigDecimal gas;

    @Column(precision = 1)
    private short stroomTariefIndicator;

    public long getDatumtijd() {
        return datumtijd;
    }

    public void setDatumtijd(long datumtijd) {
        this.datumtijd = datumtijd;
    }

    public int getStroomOpgenomenVermogenInWatt() {
        return stroomOpgenomenVermogenInWatt;
    }

    public void setStroomOpgenomenVermogenInWatt(int stroomOpgenomenVermogenInWatt) {
        this.stroomOpgenomenVermogenInWatt = stroomOpgenomenVermogenInWatt;
    }

    public BigDecimal getStroomTarief1() {
        return stroomTarief1;
    }

    public void setStroomTarief1(BigDecimal stroomTarief1) {
        this.stroomTarief1 = stroomTarief1;
    }

    public BigDecimal getStroomTarief2() {
        return stroomTarief2;
    }

    public void setStroomTarief2(BigDecimal stroomTarief2) {
        this.stroomTarief2 = stroomTarief2;
    }

    public BigDecimal getGas() {
        return gas;
    }

    public void setGas(BigDecimal gas) {
        this.gas = gas;
    }

    public StroomTariefIndicator getStroomTariefIndicator() {
        return StroomTariefIndicator.byId(this.stroomTariefIndicator);
    }

    public void setStroomTariefIndicator(StroomTariefIndicator stroomTariefIndicator) {
        this.stroomTariefIndicator = stroomTariefIndicator.getId();
    }
}
