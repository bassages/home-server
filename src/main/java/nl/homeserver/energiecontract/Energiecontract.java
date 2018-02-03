package nl.homeserver.energiecontract;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import nl.homeserver.energie.StroomTariefIndicator;

@Entity
@Table
public class Energiecontract {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    @Getter
    @Setter
    private Long van;

    @JsonIgnore
    @Column(nullable = false)
    @Getter
    @Setter
    private Long totEnMet;

    @Column(precision = 7, scale = 6, nullable = false)
    @Getter
    @Setter
    private BigDecimal stroomPerKwhNormaalTarief;

    @Column(precision = 7, scale = 6)
    @Getter
    @Setter
    private BigDecimal stroomPerKwhDalTarief;

    @Column(precision = 7, scale = 6)
    @Getter
    @Setter
    private BigDecimal gasPerKuub;

    @Getter
    @Setter
    private String leverancier;

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
}
