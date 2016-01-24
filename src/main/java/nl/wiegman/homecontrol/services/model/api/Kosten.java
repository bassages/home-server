package nl.wiegman.homecontrol.services.model.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table
public class Kosten {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique=true)
    private Long van;

    @JsonIgnore
    @Column(nullable = false)
    private Long totEnMet;

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

    public Long getVan() {
        return van;
    }

    public void setVan(Long van) {
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

    public Long getTotEnMet() {
        return totEnMet;
    }

    public void setTotEnMet(Long totEnMet) {
        this.totEnMet = totEnMet;
    }
}
