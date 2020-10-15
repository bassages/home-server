package nl.homeserver.energie.energycontract;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import nl.homeserver.energie.StroomTariefIndicator;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "ENERGIECONTRACT")
public class Energycontract {

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
    @Nullable
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

    @Getter
    @Setter
    @Nullable
    @Column(length = 2048)
    private String remark;

    public BigDecimal getStroomKosten(final StroomTariefIndicator stroomTariefIndicator) {
        return switch (stroomTariefIndicator) {
            case DAL     -> stroomPerKwhDalTarief != null ? stroomPerKwhDalTarief : stroomPerKwhNormaalTarief;
            case NORMAAL -> stroomPerKwhNormaalTarief;
            default      -> ZERO;
        };
    }
}
