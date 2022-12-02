package nl.homeserver.energy.verbruikkosten;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VerbruikKostenOverzichtenTest {

    @Test
    void whenAverageToSingleThenSingleVerbuikKostenOverichtWithAveragesReturned() {
        final VerbruikKostenOverzicht verbruikKostenOverzicht1 = VerbruikKostenOverzicht.builder()
            .gasVerbruik(new BigDecimal("42.023"))
            .gasKosten(new BigDecimal("10.000"))
            .stroomVerbruikDal(new BigDecimal("123.000"))
            .stroomKostenDal(new BigDecimal("12.872"))
            .stroomVerbruikNormaal(new BigDecimal("2450.607"))
            .stroomKostenNormaal(new BigDecimal("2312.023"))
            .build();

        final VerbruikKostenOverzicht verbruikKostenOverzicht2 = VerbruikKostenOverzicht.builder()
            .gasVerbruik(new BigDecimal("21.531"))
            .gasKosten(new BigDecimal("34.131"))
            .stroomVerbruikDal(new BigDecimal("134.012"))
            .stroomKostenDal(new BigDecimal("71.325"))
            .stroomVerbruikNormaal(new BigDecimal("2321.242"))
            .stroomKostenNormaal(new BigDecimal("9214.081"))
            .build();

        final VerbruikKostenOverzichten verbruikKostenOverzichten = new VerbruikKostenOverzichten(
                List.of(verbruikKostenOverzicht1, verbruikKostenOverzicht2));

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
        final VerbruikKostenOverzicht verbruikKostenOverzicht = VerbruikKostenOverzicht.builder()
                .stroomKostenDal(new BigDecimal(3))
                .stroomKostenNormaal(new BigDecimal(1))
                .build();

        assertThat(verbruikKostenOverzicht.getTotaalStroomKosten()).isEqualTo(new BigDecimal(4));
    }

    @Test
    void givenNullNormaalAndNotNullDalWhenGetTotaalStroomKostenThenDalReturned() {
        final VerbruikKostenOverzicht verbruikKostenOverzicht = VerbruikKostenOverzicht.builder()
                .stroomKostenDal(new BigDecimal(3))
                .stroomKostenNormaal(null)
                .build();

        assertThat(verbruikKostenOverzicht.getTotaalStroomKosten()).isEqualTo(new BigDecimal(3));
    }

    @Test
    void givenNotNullNormaalAndNullDalWhenGetTotaalStroomKostenThenNormaalReturned() {
        final VerbruikKostenOverzicht verbruikKostenOverzicht = VerbruikKostenOverzicht.builder()
                .stroomKostenDal(null)
                .stroomKostenNormaal(new BigDecimal(3))
                .build();

        assertThat(verbruikKostenOverzicht.getTotaalStroomKosten()).isEqualTo(new BigDecimal(3));
    }

    @Test
    void givenNullNormaalAndDalWhenGetTotaalStroomKostenThenNullReturned() {
        final VerbruikKostenOverzicht verbruikKostenOverzicht = VerbruikKostenOverzicht.builder()
                .stroomKostenDal(null)
                .stroomKostenNormaal(null)
                .build();

        assertThat(verbruikKostenOverzicht.getTotaalStroomKosten()).isNull();
    }
}
