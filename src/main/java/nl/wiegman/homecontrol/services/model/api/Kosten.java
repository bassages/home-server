package nl.wiegman.homecontrol.services.model.api;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table
public class Kosten {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private long van;

    @Column(nullable = false)
    private long totEnMet;

    @Column(precision = 7, scale = 6)
    private BigDecimal stroomPerKwh;

    @Column(precision = 7, scale = 6)
    private BigDecimal gasPerKuub;

    private String leverancier;

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

    public String getLeverancier() {
        return leverancier;
    }

    public void setLeverancier(String leverancier) {
        this.leverancier = leverancier;
    }

    public long getTotEnMet() {
        return totEnMet;
    }

    public void setTotEnMet(long totEnMet) {
        this.totEnMet = totEnMet;
    }
}
