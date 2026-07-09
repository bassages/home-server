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
import static org.mockito.Mockito.times;
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
        final DsmrReading dsmrReading = new DsmrReading();
        final LocalDateTime dateTime = LocalDate.of(2016, Month.NOVEMBER, 12).atTime(14, 18);
        dsmrReading.setDatumtijd(dateTime);
        final StroomTariefIndicator stroomTariefIndicator = NORMAAL;
        dsmrReading.setStroomTariefIndicator((int) stroomTariefIndicator.getId());
        dsmrReading.setGas(new BigDecimal("201.876234"));
        dsmrReading.setStroomTarief1(new BigDecimal("352.907511"));
        dsmrReading.setStroomTarief2(new BigDecimal("2341.234345"));
        dsmrReading.setStroomOpgenomenVermogenInWatt(424);
        dsmrReading.setAantalSpanningsDippenInFaseL1(100);
        dsmrReading.setAantalSpanningsDippenInFaseL2(200);
        dsmrReading.setAantalStroomStoringenInAlleFases(300);
        dsmrReading.setAantalSpanningsDippenInFaseL1(80);
        dsmrReading.setAantalSpanningsDippenInFaseL2(132);
        dsmrReading.setTekstBericht("Hello Kitty");
        dsmrReading.setTekstBerichtCodes("HK");
        dsmrReading.setMeterIdentificatieGas("METER_ID_GAS");
        dsmrReading.setMeterIdentificatieStroom("METER_ID_ELEC");
        dsmrReading.setAantalLangeStroomStoringenInAlleFases(431);

        final LangeStroomStoring langeStroomStoring = new LangeStroomStoring();
        langeStroomStoring.setDatumtijdEinde(LocalDateTime.now());
        langeStroomStoring.setDuurVanStoringInSeconden(120L);
        dsmrReading.setLangeStroomStoringen(List.of(langeStroomStoring));

        slimmeMeterController.save(dsmrReading);

        verify(meterstandService).save(meterstandCaptor.capture());
        final Meterstand savedMeterstand = meterstandCaptor.getValue();
        assertThat(savedMeterstand.getDateTime()).isEqualTo(dateTime);
        assertThat(savedMeterstand.getGas()).isEqualTo(new BigDecimal("201.876"));
        assertThat(savedMeterstand.getStroomTariefIndicator()).isEqualTo(stroomTariefIndicator);
        assertThat(savedMeterstand.getStroomTarief1()).isEqualTo(new BigDecimal("352.908"));
        assertThat(savedMeterstand.getStroomTarief2()).isEqualTo(new BigDecimal("2341.234"));
        assertThat(savedMeterstand.getMeterIdElectricity()).isEqualTo("METER_ID_ELEC");
        assertThat(savedMeterstand.getMeterIdGas()).isEqualTo("METER_ID_GAS");

        verify(opgenomenVermogenService).save(opgenomenVermogenCaptor.capture());
        assertThat(opgenomenVermogenCaptor.getValue().getDatumtijd()).isEqualTo(dateTime);
        assertThat(opgenomenVermogenCaptor.getValue().getWatt()).isEqualTo(dsmrReading.getStroomOpgenomenVermogenInWatt());
        assertThat(opgenomenVermogenCaptor.getValue().getTariefIndicator()).isEqualTo(stroomTariefIndicator);
    }

    @Test
    void whenSaveTwiceWithinTenSecondsThenSecondReadingNotSaved() {
        // given
        final LocalDateTime firstReadingTime = LocalDate.of(2016, Month.NOVEMBER, 12).atTime(14, 18);
        final DsmrReading firstDsmrReading = createBasicDsmrReading(firstReadingTime);

        final LocalDateTime secondReadingTime = firstReadingTime.plusSeconds(9);
        final DsmrReading secondDsmrReading = createBasicDsmrReading(secondReadingTime);

        // when
        slimmeMeterController.save(firstDsmrReading);
        slimmeMeterController.save(secondDsmrReading);

        // then
        verify(meterstandService, times(1)).save(meterstandCaptor.capture());
        verify(opgenomenVermogenService, times(1)).save(opgenomenVermogenCaptor.capture());

        assertThat(meterstandCaptor.getValue().getDateTime()).isEqualTo(firstReadingTime);
        assertThat(meterstandCaptor.getValue().getMeterIdElectricity()).isEqualTo("METER_ID_ELEC");
        assertThat(meterstandCaptor.getValue().getMeterIdGas()).isEqualTo("METER_ID_GAS");
        assertThat(opgenomenVermogenCaptor.getValue().getDatumtijd()).isEqualTo(firstReadingTime);
    }

    @CaptureLogging(SlimmeMeterController.class)
    @Test
    void whenSaveThenLoggedAtLevelInfo(final ArgumentCaptor<LoggingEvent> loggerEventCaptor) {
        // given
        final LocalDateTime now = LocalDate.of(2016, Month.NOVEMBER, 12).atTime(14, 18);

        final DsmrReading dsmrReading = new DsmrReading();
        dsmrReading.setDatumtijd(now);
        dsmrReading.setStroomTariefIndicator((int) NORMAAL.getId());
        dsmrReading.setGas(TEN);
        dsmrReading.setStroomTarief1(TEN);
        dsmrReading.setStroomTarief2(TEN);

        // when
        slimmeMeterController.save(dsmrReading);

        // then
        final LoggingEvent loggingEvent = loggerEventCaptor.getValue();
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(loggingEvent.getFormattedMessage()).startsWith("DsmrReading(");
    }

    private DsmrReading createBasicDsmrReading(final LocalDateTime localDateTime) {
        final DsmrReading dsmrReading = new DsmrReading();
        dsmrReading.setDatumtijd(localDateTime);
        dsmrReading.setStroomTariefIndicator((int) NORMAAL.getId());
        dsmrReading.setGas(TEN);
        dsmrReading.setStroomTarief1(TEN);
        dsmrReading.setStroomTarief2(TEN);
        dsmrReading.setMeterIdentificatieGas("METER_ID_GAS");
        dsmrReading.setMeterIdentificatieStroom("METER_ID_ELEC");
        return dsmrReading;
    }
}
