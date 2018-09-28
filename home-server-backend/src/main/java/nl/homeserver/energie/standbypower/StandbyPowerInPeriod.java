package nl.homeserver.energie.standbypower;

import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import lombok.Getter;
import nl.homeserver.energie.verbruikkosten.VerbruikKostenOverzicht;

class StandbyPowerInPeriod {

    @Getter
    private int month;
    @Getter
    private int year;
    @Getter
    private LocalDate fromDate;
    @Getter
    private LocalDate toDate;
    @Getter
    private BigDecimal percentageOfTotalPeriod;

    @Getter
    private Integer standbyPower;
    @Getter
    private BigDecimal costsOfStandByPower;
    @Getter
    private BigDecimal totalCostsOfPower;
    @Getter
    private BigDecimal percentageOfTotalCost;

    StandbyPowerInPeriod(final YearMonth yearMonth,
                         final int standbyPower,
                         final BigDecimal percentageOfTotalPeriod,
                         final VerbruikKostenOverzicht standByPowerVko,
                         final VerbruikKostenOverzicht actualVko) {
        this.month = yearMonth.getMonthValue();
        this.year = yearMonth.getYear();
        this.fromDate = yearMonth.atDay(1);
        this.toDate = yearMonth.atEndOfMonth().plusDays(1);
        this.standbyPower = standbyPower;
        this.percentageOfTotalPeriod = percentageOfTotalPeriod;

        this.costsOfStandByPower = standByPowerVko.getTotaalStroomKosten();
        this.totalCostsOfPower = actualVko.getTotaalStroomKosten();

        percentageOfTotalCost = costsOfStandByPower.divide(totalCostsOfPower, 2, HALF_UP)
                                                   .multiply(BigDecimal.valueOf(100)).setScale(0, HALF_UP);

    }

    private StandbyPowerInPeriod() {
        // Private zero args constructor
    }
}
