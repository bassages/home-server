package nl.wiegman.homecontrol.services.model.api;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table
public class Kosten {

    @Id
    private long id;

    private long van;
    private long totEnMet;

    private BigDecimal stroomPerKwh;
    private BigDecimal gasPerKuub;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVan() {
        return van;
    }

    public void setVan(long van) {
        this.van = van;
    }

    public long getTotEnMet() {
        return totEnMet;
    }

    public void setTot(long totEnMet) {
        this.totEnMet = totEnMet;
    }

    public BigDecimal getStroomPerKwh() {
        return stroomPerKwh;
    }

    public void setStroomPerKwh(BigDecimal stroomPerKwh) {
        this.stroomPerKwh = stroomPerKwh;
    }

    public BigDecimal getGasPerKuub() {
        return gasPerKuub;
    }

    public void setGasPerKuub(BigDecimal gasPerKuub) {
        this.gasPerKuub = gasPerKuub;
    }
}
