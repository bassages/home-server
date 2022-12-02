package nl.homeserver.energy.energycontract;

import java.math.BigDecimal;
import java.time.LocalDate;

record EnergyContractDto (
        Long id,
        LocalDate validFrom,
        LocalDate validTo,
        BigDecimal electricityPerKwhStandardTariff,
        BigDecimal electricityPerKwhOffPeakTariff,
        BigDecimal gasPerCubicMeter,
        String supplierName,
        String remark
) { }
