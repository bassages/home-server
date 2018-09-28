package nl.homeserver.energie.energiecontract;

import static nl.homeserver.energie.StroomTariefIndicator.DAL;
import static nl.homeserver.energie.StroomTariefIndicator.NORMAAL;
import static nl.homeserver.energie.StroomTariefIndicator.ONBEKEND;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnergiecontractTest {

    @Test
    public void givenStroomPerKwhNormaalTariefSetWhenGetStroomKostenNormaalThenReturned() {
        final Energiecontract energiecontract = new Energiecontract();
        final BigDecimal normaalTarief = new BigDecimal("20.121231");
        energiecontract.setStroomPerKwhNormaalTarief(normaalTarief);

        assertThat(energiecontract.getStroomKosten(NORMAAL)).isSameAs(normaalTarief);
    }

    @Test
    public void givenStroomPerKwhDalTariefSetWhenGetStroomKostenDalThenReturned() {
        final Energiecontract energiecontract = new Energiecontract();
        final BigDecimal dalTarief = new BigDecimal("785.7453");
        energiecontract.setStroomPerKwhDalTarief(dalTarief);

        assertThat(energiecontract.getStroomKosten(DAL)).isSameAs(dalTarief);
    }

    @Test
    public void givenStroomPerKwhDalTariefNullWhenGetStroomKostenDalThenNormaalReturned() {
        final Energiecontract energiecontract = new Energiecontract();
        final BigDecimal normaalTarief = new BigDecimal("123.121231");
        energiecontract.setStroomPerKwhNormaalTarief(normaalTarief);

        assertThat(energiecontract.getStroomKosten(DAL)).isSameAs(normaalTarief);
    }

    @Test
    public void givenWhenGetStroomHostenOnbekendThenZeroReturned() {
        final Energiecontract energiecontract = new Energiecontract();

        assertThat(energiecontract.getStroomKosten(ONBEKEND)).isSameAs(BigDecimal.ZERO);
    }

}