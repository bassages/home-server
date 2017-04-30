package nl.wiegman.home.energie.energiecontract;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import nl.wiegman.home.energie.Energiesoort;

@Entity
@Table
public class Energiecontract {

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

    public BigDecimal getKosten(Energiesoort energiesoort) {
        switch (energiesoort) {
            case GAS:
                return gasPerKuub;
            case STROOM:
                return stroomPerKwh;
            default:
                return BigDecimal.ZERO;
        }
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
