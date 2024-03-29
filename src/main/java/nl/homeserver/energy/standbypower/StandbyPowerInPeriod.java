package nl.homeserver.energy.standbypower;

import lombok.Getter;
import lombok.NoArgsConstructor;
import nl.homeserver.DatePeriod;
import nl.homeserver.energy.verbruikkosten.VerbruikKostenOverzicht;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.math.RoundingMode.HALF_UP;
import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor(access = PRIVATE)
class StandbyPowerInPeriod {

    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal percentageOfTotalPeriod;

    private Integer standbyPower;
    private Integer approxYearlyUsageKwh;
    private BigDecimal costsOfStandByPower;
    private BigDecimal totalCostsOfPower;
    private BigDecimal percentageOfTotalCost;

    StandbyPowerInPeriod(final DatePeriod period,
                         final int standbyPower,
                         final int approxYearlyUsageKwh,
                         final BigDecimal percentageOfTotalPeriod,
                         final VerbruikKostenOverzicht standByPowerVko,
                         final VerbruikKostenOverzicht actualVko) {
        this.fromDate = period.getFromDate();
        this.toDate = period.getToDate();
        this.standbyPower = standbyPower;
        this.approxYearlyUsageKwh = approxYearlyUsageKwh;
        this.percentageOfTotalPeriod = percentageOfTotalPeriod;

        this.costsOfStandByPower = standByPowerVko.getTotaalStroomKosten();
        this.totalCostsOfPower = actualVko.getTotaalStroomKosten();

        if  (this.costsOfStandByPower != null && this.totalCostsOfPower != null) {
            percentageOfTotalCost = costsOfStandByPower.divide(totalCostsOfPower, 2, HALF_UP)
                                                       .multiply(BigDecimal.valueOf(100)).setScale(0, HALF_UP);

        }
    }
}
