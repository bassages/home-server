package nl.wiegman.home.energiecontract;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import nl.wiegman.home.energie.StroomTariefIndicator;

@Entity
@Table
public class Energiecontract {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    private Long van;

    @JsonIgnore
    @Column(nullable = false)
    private Long totEnMet;

    @Column(precision = 7, scale = 6, nullable = false)
    private BigDecimal stroomPerKwhNormaalTarief;

    @Column(precision = 7, scale = 6)
    private BigDecimal stroomPerKwhDalTarief;

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

    public BigDecimal getStroomKosten(StroomTariefIndicator stroomTariefIndicator) {
        switch (stroomTariefIndicator) {
            case DAL:
                if (stroomPerKwhDalTarief != null) {
                    return stroomPerKwhDalTarief;
                } else {
                    return stroomPerKwhNormaalTarief;
                }
            case NORMAAL:
                return stroomPerKwhNormaalTarief;
            default:
                return ZERO;
        }
    }

    public BigDecimal getStroomPerKwhNormaalTarief() {
        return stroomPerKwhNormaalTarief;
    }

    public void setStroomPerKwhNormaalTarief(BigDecimal stroomPerKwhNormaalTarief) {
        this.stroomPerKwhNormaalTarief = stroomPerKwhNormaalTarief;
    }

    public BigDecimal getStroomPerKwhDalTarief() {
        return stroomPerKwhDalTarief;
    }

    public void setStroomPerKwhDalTarief(BigDecimal stroomPerKwhDalTarief) {
        this.stroomPerKwhDalTarief = stroomPerKwhDalTarief;
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
