package nl.homeserver.energie.energycontract;

import static nl.homeserver.energie.StroomTariefIndicator.DAL;
import static nl.homeserver.energie.StroomTariefIndicator.NORMAAL;
import static nl.homeserver.energie.StroomTariefIndicator.ONBEKEND;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnergyContractTest {

    @Test
    void givenStroomPerKwhNormaalTariefSetWhenGetStroomKostenNormaalThenReturned() {
        final EnergyContract energyContract = new EnergyContract();
        final BigDecimal normaalTarief = new BigDecimal("20.121231");
        energyContract.setStroomPerKwhNormaalTarief(normaalTarief);

        assertThat(energyContract.getStroomKosten(NORMAAL)).isSameAs(normaalTarief);
    }

    @Test
    void givenStroomPerKwhDalTariefSetWhenGetStroomKostenDalThenReturned() {
        final EnergyContract energyContract = new EnergyContract();
        final BigDecimal dalTarief = new BigDecimal("785.7453");
        energyContract.setStroomPerKwhDalTarief(dalTarief);

        assertThat(energyContract.getStroomKosten(DAL)).isSameAs(dalTarief);
    }

    @Test
    void givenStroomPerKwhDalTariefNullWhenGetStroomKostenDalThenNormaalReturned() {
        final EnergyContract energyContract = new EnergyContract();
        final BigDecimal normaalTarief = new BigDecimal("123.121231");
        energyContract.setStroomPerKwhNormaalTarief(normaalTarief);

        assertThat(energyContract.getStroomKosten(DAL)).isSameAs(normaalTarief);
    }

    @Test
    void whenGetStroomHostenOnbekendThenZeroReturned() {
        final EnergyContract energyContract = new EnergyContract();

        assertThat(energyContract.getStroomKosten(ONBEKEND)).isSameAs(BigDecimal.ZERO);
    }
}
