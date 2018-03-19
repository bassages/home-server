package nl.homeserver.energie;

import static ch.qos.logback.classic.Level.INFO;
import static ch.qos.logback.classic.Level.OFF;
import static java.math.BigDecimal.TEN;
import static java.util.Collections.singletonList;
import static nl.homeserver.energie.StroomTariefIndicator.NORMAAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homeserver.LoggingRule;

@RunWith(MockitoJUnitRunner.class)
public class SlimmeMeterControllerTest {

    @InjectMocks
    private SlimmeMeterController slimmeMeterController;

    @Mock
    private OpgenomenVermogenService opgenomenVermogenService;
    @Mock
    private MeterstandService meterstandService;

    @Rule
    public LoggingRule loggingRule = new LoggingRule(SlimmeMeterController.class);

    @Captor
    private ArgumentCaptor<Meterstand> meterstandCaptor;
    @Captor
    private ArgumentCaptor<OpgenomenVermogen> opgenomenVermogenCaptor;

    @Test
    public void whenSaveThenMeterstandAndOpgenomenVermogenSaved() {
        Dsmr42Reading dsmr42Reading = new Dsmr42Reading();
        LocalDateTime dateTime = LocalDate.of(2016, Month.NOVEMBER, 12).atTime(14, 18);
        dsmr42Reading.setDatumtijd(dateTime);
        StroomTariefIndicator stroomTariefIndicator = NORMAAL;
        dsmr42Reading.setStroomTariefIndicator((int) stroomTariefIndicator.getId());
        dsmr42Reading.setGas(new BigDecimal("201.876234"));
        dsmr42Reading.setStroomTarief1(new BigDecimal("352.907511"));
        dsmr42Reading.setStroomTarief2(new BigDecimal("2341.234345"));
        dsmr42Reading.setStroomOpgenomenVermogenInWatt(424);
        dsmr42Reading.setAantalSpanningsDippenInFaseL1(100);
        dsmr42Reading.setAantalSpanningsDippenInFaseL2(200);
        dsmr42Reading.setAantalStroomStoringenInAlleFases(300);
        dsmr42Reading.setAantalSpanningsDippenInFaseL1(80);
        dsmr42Reading.setAantalSpanningsDippenInFaseL2(132);
        dsmr42Reading.setTekstBericht("Hello Kitty");
        dsmr42Reading.setTekstBerichtCodes("HK");
        dsmr42Reading.setMeterIdentificatieGas("MIG");
        dsmr42Reading.setMeterIdentificatieStroom("MIS");
        dsmr42Reading.setAantalLangeStroomStoringenInAlleFases(431);

        LangeStroomStoring langeStroomStoring = new LangeStroomStoring();
        langeStroomStoring.setDatumtijdEinde(LocalDateTime.now());
        langeStroomStoring.setDuurVanStoringInSeconden(120L);
        dsmr42Reading.setLangeStroomStoringen(singletonList(langeStroomStoring));

        slimmeMeterController.save(dsmr42Reading);

        verify(meterstandService).save(meterstandCaptor.capture());
        Meterstand savedMeterstand = meterstandCaptor.getValue();
        assertThat(savedMeterstand.getDateTime()).isEqualTo(dateTime);
        assertThat(savedMeterstand.getGas()).isEqualTo(new BigDecimal("201.876"));
        assertThat(savedMeterstand.getStroomTariefIndicator()).isEqualTo(stroomTariefIndicator);
        assertThat(savedMeterstand.getStroomTarief1()).isEqualTo(new BigDecimal("352.908"));
        assertThat(savedMeterstand.getStroomTarief2()).isEqualTo(new BigDecimal("2341.234"));

        verify(opgenomenVermogenService).save(opgenomenVermogenCaptor.capture());
        assertThat(opgenomenVermogenCaptor.getValue().getDatumtijd()).isEqualTo(dateTime);
        assertThat(opgenomenVermogenCaptor.getValue().getWatt()).isEqualTo(dsmr42Reading.getStroomOpgenomenVermogenInWatt());
        assertThat(opgenomenVermogenCaptor.getValue().getTariefIndicator()).isEqualTo(stroomTariefIndicator);
    }

    @Test
    public void givenLogLevelIsInfoWhenSaveThenLoggedAtLevelInfo() {
        Dsmr42Reading dsmr42Reading = new Dsmr42Reading();
        dsmr42Reading.setStroomTariefIndicator((int) NORMAAL.getId());
        dsmr42Reading.setGas(TEN);
        dsmr42Reading.setStroomTarief1(TEN);
        dsmr42Reading.setStroomTarief2(TEN);

        loggingRule.setLevel(INFO);

        slimmeMeterController.save(dsmr42Reading);

        LoggingEvent loggingEvent = loggingRule.getLoggedEventCaptor().getValue();
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(loggingEvent.getFormattedMessage()).startsWith("nl.homeserver.energie.Dsmr42Reading");
    }

    @Test
    public void givenLogLevelIsOffWhenSaveThenNothingLogged() {
        Dsmr42Reading dsmr42Reading = new Dsmr42Reading();
        dsmr42Reading.setStroomTariefIndicator((int) NORMAAL.getId());
        dsmr42Reading.setGas(TEN);
        dsmr42Reading.setStroomTarief1(TEN);
        dsmr42Reading.setStroomTarief2(TEN);

        loggingRule.setLevel(OFF);

        slimmeMeterController.save(dsmr42Reading);

        assertThat(loggingRule.getLoggedEventCaptor().getAllValues()).isEmpty();
    }

}