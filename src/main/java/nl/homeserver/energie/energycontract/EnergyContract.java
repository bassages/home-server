package nl.homeserver.energie.energycontract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import nl.homeserver.energie.StroomTariefIndicator;

import javax.annotation.Nullable;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import static java.math.BigDecimal.ZERO;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "ENERGIECONTRACT")
public class EnergyContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate validFrom;

    @JsonIgnore
    @Nullable
    private LocalDate validTo;

    @Column(precision = 7, scale = 6, nullable = false)
    private BigDecimal stroomPerKwhNormaalTarief;

    @Column(precision = 7, scale = 6)
    private BigDecimal stroomPerKwhDalTarief;

    @Column(precision = 7, scale = 6)
    private BigDecimal gasPerKuub;

    private String leverancier;

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
