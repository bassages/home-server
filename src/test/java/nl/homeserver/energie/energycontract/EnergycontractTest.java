package nl.homeserver.energie.energycontract;

import static nl.homeserver.energie.StroomTariefIndicator.DAL;
import static nl.homeserver.energie.StroomTariefIndicator.NORMAAL;
import static nl.homeserver.energie.StroomTariefIndicator.ONBEKEND;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnergycontractTest {

    @Test
    public void givenStroomPerKwhNormaalTariefSetWhenGetStroomKostenNormaalThenReturned() {
        final Energycontract energycontract = new Energycontract();
        final BigDecimal normaalTarief = new BigDecimal("20.121231");
        energycontract.setStroomPerKwhNormaalTarief(normaalTarief);

        assertThat(energycontract.getStroomKosten(NORMAAL)).isSameAs(normaalTarief);
    }

    @Test
    public void givenStroomPerKwhDalTariefSetWhenGetStroomKostenDalThenReturned() {
        final Energycontract energycontract = new Energycontract();
        final BigDecimal dalTarief = new BigDecimal("785.7453");
        energycontract.setStroomPerKwhDalTarief(dalTarief);

        assertThat(energycontract.getStroomKosten(DAL)).isSameAs(dalTarief);
    }

    @Test
    public void givenStroomPerKwhDalTariefNullWhenGetStroomKostenDalThenNormaalReturned() {
        final Energycontract energycontract = new Energycontract();
        final BigDecimal normaalTarief = new BigDecimal("123.121231");
        energycontract.setStroomPerKwhNormaalTarief(normaalTarief);

        assertThat(energycontract.getStroomKosten(DAL)).isSameAs(normaalTarief);
    }

    @Test
    public void givenWhenGetStroomHostenOnbekendThenZeroReturned() {
        final Energycontract energycontract = new Energycontract();

        assertThat(energycontract.getStroomKosten(ONBEKEND)).isSameAs(BigDecimal.ZERO);
    }

}
