package nl.homeserver.energie.energycontract;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.annotation.Nullable;

@SuppressWarnings({ "FieldMayBeFinal", "WeakerAccess", "CanBeFinal" })
public class EnergiecontractBuilder {

    private BigDecimal gasPerKuub = ZERO;
    private BigDecimal stroomPerKwhNormaalTarief = ZERO;
    private BigDecimal stroomPerKwhDalTarief = ZERO;
    private LocalDate validFrom;
    private LocalDate validTo;

    public static EnergiecontractBuilder anEnergiecontract() {
        return new EnergiecontractBuilder();
    }

    public EnergiecontractBuilder withValidFrom(@Nullable final LocalDate validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    public EnergiecontractBuilder withValidTo(@Nullable final LocalDate validTo) {
        this.validTo = validTo;
        return this;
    }

    public EnergyContract build() {
        final EnergyContract energyContract = new EnergyContract();
        energyContract.setGasPerKuub(gasPerKuub);
        energyContract.setStroomPerKwhNormaalTarief(stroomPerKwhNormaalTarief);
        energyContract.setStroomPerKwhDalTarief(stroomPerKwhDalTarief);
        energyContract.setValidFrom(validFrom);
        energyContract.setValidTo(validTo);
        return energyContract;
    }
}
