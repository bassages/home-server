package nl.homeserver.energy.energycontract;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static nl.homeserver.energy.StroomTariefIndicator.*;
import static org.assertj.core.api.Assertions.assertThat;

class EnergyContractTest {

    @Test
    void givenStandardTariffSetWhenGetStandardTariffThenReturned() {
        final EnergyContract energyContract = new EnergyContract();
        final BigDecimal standardTariff = new BigDecimal("20.121231");
        energyContract.setElectricityPerKwhStandardTariff(standardTariff);

        assertThat(energyContract.getElectricityCost(NORMAAL)).isSameAs(standardTariff);
    }

    @Test
    void givenOffPeakTariffSetWhenGetOffPeakTariffThenReturned() {
        final EnergyContract energyContract = new EnergyContract();
        final BigDecimal offPeakTariff = new BigDecimal("785.7453");
        energyContract.setElectricityPerKwhOffPeakTariff(offPeakTariff);

        assertThat(energyContract.getElectricityCost(DAL)).isSameAs(offPeakTariff);
    }

    @Test
    void givenElectricityPerKwhOffPeakTariffNullWhenGetElectricityCostOffPeakThenStandardTariffReturned() {
        final EnergyContract energyContract = new EnergyContract();
        final BigDecimal standardTariff = new BigDecimal("123.121231");
        energyContract.setElectricityPerKwhStandardTariff(standardTariff);

        assertThat(energyContract.getElectricityCost(DAL)).isSameAs(standardTariff);
    }

    @Test
    void whenGetStroomHostenOnbekendThenZeroReturned() {
        final EnergyContract energyContract = new EnergyContract();

        assertThat(energyContract.getElectricityCost(ONBEKEND)).isSameAs(BigDecimal.ZERO);
    }
}
