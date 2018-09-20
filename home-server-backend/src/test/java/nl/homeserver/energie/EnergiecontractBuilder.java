package nl.homeserver.energie;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.time.LocalDate;

import nl.homeserver.energiecontract.Energiecontract;

public class EnergiecontractBuilder {

    private BigDecimal gasPerKuub = ZERO;
    private BigDecimal stroomPerKwhNormaalTarief = ZERO;
    private BigDecimal stroomPerKwhDalTarief = ZERO;
    private LocalDate validFrom;
    private LocalDate validTo;

    public static EnergiecontractBuilder anEnergiecontract() {
        return new EnergiecontractBuilder();
    }

    public EnergiecontractBuilder withValidFrom(final LocalDate validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    public EnergiecontractBuilder withValidTo(final LocalDate validTo) {
        this.validTo = validTo;
        return this;
    }

    public Energiecontract build() {
        final Energiecontract energiecontract = new Energiecontract();
        energiecontract.setGasPerKuub(gasPerKuub);
        energiecontract.setStroomPerKwhNormaalTarief(stroomPerKwhNormaalTarief);
        energiecontract.setStroomPerKwhDalTarief(stroomPerKwhDalTarief);
        energiecontract.setValidFrom(validFrom);
        energiecontract.setValidTo(validTo);
        return energiecontract;
    }
}
