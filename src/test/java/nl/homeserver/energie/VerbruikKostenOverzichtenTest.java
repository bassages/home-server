package nl.homeserver.energie;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.Test;

public class VerbruikKostenOverzichtenTest {

    @Test
    public void whenGetAveragesThenAveragesAreReturned() {
        VerbruikKostenOverzicht verbruikKostenOverzicht1 = new VerbruikKostenOverzicht();
        verbruikKostenOverzicht1.setGasVerbruik(new BigDecimal(42.023));
        verbruikKostenOverzicht1.setGasKosten(new BigDecimal(10.000));
        verbruikKostenOverzicht1.setStroomVerbruikDal(new BigDecimal(123.000));
        verbruikKostenOverzicht1.setStroomKostenDal(new BigDecimal(12.872));
        verbruikKostenOverzicht1.setStroomVerbruikNormaal(new BigDecimal(2450.607));
        verbruikKostenOverzicht1.setStroomKostenNormaal(new BigDecimal(2312.023));

        VerbruikKostenOverzicht verbruikKostenOverzicht2 = new VerbruikKostenOverzicht();
        verbruikKostenOverzicht2.setGasVerbruik(new BigDecimal(21.531));
        verbruikKostenOverzicht2.setGasKosten(new BigDecimal(34.131));
        verbruikKostenOverzicht2.setStroomVerbruikDal(new BigDecimal(134.012));
        verbruikKostenOverzicht2.setStroomKostenDal(new BigDecimal(71.325));
        verbruikKostenOverzicht2.setStroomVerbruikNormaal(new BigDecimal(2321.242));
        verbruikKostenOverzicht2.setStroomKostenNormaal(new BigDecimal(9214.081));

        VerbruikKostenOverzichten verbruikKostenOverzichten = new VerbruikKostenOverzichten(asList(verbruikKostenOverzicht1, verbruikKostenOverzicht2));
        VerbruikKostenOverzicht averagedVerbruikKostenOverzicht = verbruikKostenOverzichten.getAverages();

        assertThat(averagedVerbruikKostenOverzicht.getGasVerbruik()).isEqualTo(new BigDecimal("31.777"));
        assertThat(averagedVerbruikKostenOverzicht.getStroomVerbruikDal()).isEqualTo(new BigDecimal("128.506"));
        assertThat(averagedVerbruikKostenOverzicht.getStroomVerbruikNormaal()).isEqualTo(new BigDecimal("2385.925"));
        assertThat(averagedVerbruikKostenOverzicht.getGasKosten()).isEqualTo(new BigDecimal("22.07"));
        assertThat(averagedVerbruikKostenOverzicht.getStroomKostenDal()).isEqualTo(new BigDecimal("42.10"));
        assertThat(averagedVerbruikKostenOverzicht.getStroomKostenNormaal()).isEqualTo(new BigDecimal("5763.05"));
    }

}