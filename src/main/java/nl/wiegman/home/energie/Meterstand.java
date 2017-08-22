package nl.wiegman.home.energie;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Meterstand {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    private long datumtijd;

    @Column(nullable = false, precision = 8, scale = 3)
    private BigDecimal stroomTarief1;

    @Column(nullable = false, precision = 8, scale = 3)
    private BigDecimal stroomTarief2;

    @Column(nullable = false, precision = 8, scale = 3)
    private BigDecimal gas;

    @Column(nullable = false, precision = 1)
    private short stroomTariefIndicator;

    public long getDatumtijd() {
        return datumtijd;
    }

    @JsonIgnore
    public Date getDatumtijdAsDate() {
        return new Date(datumtijd);
    }

    public void setDatumtijd(long datumtijd) {
        this.datumtijd = datumtijd;
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
