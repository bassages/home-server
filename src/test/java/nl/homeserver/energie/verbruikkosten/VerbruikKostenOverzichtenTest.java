package nl.homeserver.energie.verbruikkosten;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

class VerbruikKostenOverzichtenTest {

    @Test
    void whenAverageToSingleThenSingleVerbuikKostenOverichtWithAveragesReturned() {
        final VerbruikKostenOverzicht verbruikKostenOverzicht1 = new VerbruikKostenOverzicht();
        verbruikKostenOverzicht1.setGasVerbruik(new BigDecimal("42.023"));
        verbruikKostenOverzicht1.setGasKosten(new BigDecimal("10.000"));
        verbruikKostenOverzicht1.setStroomVerbruikDal(new BigDecimal("123.000"));
        verbruikKostenOverzicht1.setStroomKostenDal(new BigDecimal("12.872"));
        verbruikKostenOverzicht1.setStroomVerbruikNormaal(new BigDecimal("2450.607"));
        verbruikKostenOverzicht1.setStroomKostenNormaal(new BigDecimal("2312.023"));

        final VerbruikKostenOverzicht verbruikKostenOverzicht2 = new VerbruikKostenOverzicht();
        verbruikKostenOverzicht2.setGasVerbruik(new BigDecimal("21.531"));
        verbruikKostenOverzicht2.setGasKosten(new BigDecimal("34.131"));
        verbruikKostenOverzicht2.setStroomVerbruikDal(new BigDecimal("134.012"));
        verbruikKostenOverzicht2.setStroomKostenDal(new BigDecimal("71.325"));
        verbruikKostenOverzicht2.setStroomVerbruikNormaal(new BigDecimal("2321.242"));
        verbruikKostenOverzicht2.setStroomKostenNormaal(new BigDecimal("9214.081"));

        final VerbruikKostenOverzichten verbruikKostenOverzichten = new VerbruikKostenOverzichten(List.of(verbruikKostenOverzicht1, verbruikKostenOverzicht2));

        final VerbruikKostenOverzicht averagedVerbruikKostenOverzicht = verbruikKostenOverzichten.averageToSingle();

        assertThat(averagedVerbruikKostenOverzicht.getGasVerbruik()).isEqualTo(new BigDecimal("31.777"));
        assertThat(averagedVerbruikKostenOverzicht.getStroomVerbruikDal()).isEqualTo(new BigDecimal("128.506"));
        assertThat(averagedVerbruikKostenOverzicht.getStroomVerbruikNormaal()).isEqualTo(new BigDecimal("2385.925"));
        assertThat(averagedVerbruikKostenOverzicht.getGasKosten()).isEqualTo(new BigDecimal("22.07"));
        assertThat(averagedVerbruikKostenOverzicht.getStroomKostenDal()).isEqualTo(new BigDecimal("42.10"));
        assertThat(averagedVerbruikKostenOverzicht.getStroomKostenNormaal()).isEqualTo(new BigDecimal("5763.05"));
    }

    @Test
    void givenNotNullNormaalAndNotNullDalWhenGetTotaalStroomKostenThenSumOfNormaalAndDalReturned() {
        final VerbruikKostenOverzicht verbruikKostenOverzicht = new VerbruikKostenOverzicht();
        verbruikKostenOverzicht.setStroomKostenDal(new BigDecimal(3));
        verbruikKostenOverzicht.setStroomKostenNormaal(new BigDecimal(1));

        assertThat(verbruikKostenOverzicht.getTotaalStroomKosten()).isEqualTo(new BigDecimal(4));
    }

    @Test
    void givenNullNormaalAndNotNullDalWhenGetTotaalStroomKostenThenDalReturned() {
        final VerbruikKostenOverzicht verbruikKostenOverzicht = new VerbruikKostenOverzicht();
        verbruikKostenOverzicht.setStroomKostenDal(new BigDecimal(3));
        verbruikKostenOverzicht.setStroomKostenNormaal(null);

        assertThat(verbruikKostenOverzicht.getTotaalStroomKosten()).isEqualTo(new BigDecimal(3));
    }

    @Test
    void givenNotNullNormaalAndNullDalWhenGetTotaalStroomKostenThenNormaalReturned() {
        final VerbruikKostenOverzicht verbruikKostenOverzicht = new VerbruikKostenOverzicht();
        verbruikKostenOverzicht.setStroomKostenDal(null);
        verbruikKostenOverzicht.setStroomKostenNormaal(new BigDecimal(3));

        assertThat(verbruikKostenOverzicht.getTotaalStroomKosten()).isEqualTo(new BigDecimal(3));
    }

    @Test
    void givenNullNormaalAndDalWhenGetTotaalStroomKostenThenNullReturned() {
        final VerbruikKostenOverzicht verbruikKostenOverzicht = new VerbruikKostenOverzicht();
        verbruikKostenOverzicht.setStroomKostenDal(null);
        verbruikKostenOverzicht.setStroomKostenNormaal(null);

        assertThat(verbruikKostenOverzicht.getTotaalStroomKosten()).isNull();
    }
}
