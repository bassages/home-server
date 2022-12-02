package nl.homeserver.energy.energycontract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import nl.homeserver.energy.StroomTariefIndicator;

import javax.annotation.Nullable;
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
    private BigDecimal electricityPerKwhStandardTariff;

    @Column(precision = 7, scale = 6)
    private BigDecimal electricityPerKwhOffPeakTariff;

    @Column(precision = 7, scale = 6)
    private BigDecimal gasPerCubicMeter;

    private String supplierName;

    @Nullable
    @Column(length = 2048)
    private String remark;

    public BigDecimal getElectricityCost(final StroomTariefIndicator stroomTariefIndicator) {
        return switch (stroomTariefIndicator) {
            case DAL     -> electricityPerKwhOffPeakTariff != null ? electricityPerKwhOffPeakTariff : electricityPerKwhStandardTariff;
            case NORMAAL -> electricityPerKwhStandardTariff;
            default      -> ZERO;
        };
    }
}
