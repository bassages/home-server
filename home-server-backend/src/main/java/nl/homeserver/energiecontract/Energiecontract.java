package nl.homeserver.energiecontract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import nl.homeserver.energie.StroomTariefIndicator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import static java.math.BigDecimal.ZERO;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table
public class Energiecontract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Column(nullable = false, unique = true)
    @Getter
    @Setter
    private LocalDate validFrom;

    @JsonIgnore
    @Getter
    @Setter
    private LocalDate validTo;

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

    public BigDecimal getStroomKosten(final StroomTariefIndicator stroomTariefIndicator) {
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
