package nl.homeserver.energy.slimmemeter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homeserver.CaptureLogging;
import nl.homeserver.energy.StroomTariefIndicator;
import nl.homeserver.energy.meterreading.Meterstand;
import nl.homeserver.energy.meterreading.MeterstandService;
import nl.homeserver.energy.opgenomenvermogen.OpgenomenVermogen;
import nl.homeserver.energy.opgenomenvermogen.OpgenomenVermogenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static java.math.BigDecimal.TEN;
import static nl.homeserver.energy.StroomTariefIndicator.NORMAAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SlimmeMeterControllerTest {

    @InjectMocks
    SlimmeMeterController slimmeMeterController;

    @Mock
    OpgenomenVermogenService opgenomenVermogenService;
    @Mock
    MeterstandService meterstandService;

    @Captor
    ArgumentCaptor<Meterstand> meterstandCaptor;
    @Captor
    ArgumentCaptor<OpgenomenVermogen> opgenomenVermogenCaptor;

    @Test
    void whenSaveThenMeterstandAndOpgenomenVermogenSaved() {
        final Dsmr42Reading dsmr42Reading = new Dsmr42Reading();
        final LocalDateTime dateTime = LocalDate.of(2016, Month.NOVEMBER, 12).atTime(14, 18);
        dsmr42Reading.setDatumtijd(dateTime);
        final StroomTariefIndicator stroomTariefIndicator = NORMAAL;
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

        final LangeStroomStoring langeStroomStoring = new LangeStroomStoring();
        langeStroomStoring.setDatumtijdEinde(LocalDateTime.now());
        langeStroomStoring.setDuurVanStoringInSeconden(120L);
        dsmr42Reading.setLangeStroomStoringen(List.of(langeStroomStoring));

        slimmeMeterController.save(dsmr42Reading);

        verify(meterstandService).save(meterstandCaptor.capture());
        final Meterstand savedMeterstand = meterstandCaptor.getValue();
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

    @CaptureLogging(SlimmeMeterController.class)
    @Test
    void whenSaveThenLoggedAtLevelInfo(final ArgumentCaptor<LoggingEvent> loggerEventCaptor) {
        // given
        final Dsmr42Reading dsmr42Reading = new Dsmr42Reading();
        dsmr42Reading.setStroomTariefIndicator((int) NORMAAL.getId());
        dsmr42Reading.setGas(TEN);
        dsmr42Reading.setStroomTarief1(TEN);
        dsmr42Reading.setStroomTarief2(TEN);

        // when
        slimmeMeterController.save(dsmr42Reading);

        // then
        final LoggingEvent loggingEvent = loggerEventCaptor.getValue();
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(loggingEvent.getFormattedMessage()).startsWith("Dsmr42Reading(");
    }
}
