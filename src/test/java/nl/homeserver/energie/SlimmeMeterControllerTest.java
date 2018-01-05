package nl.homeserver.energie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.homeserver.DateTimeUtil;

@RunWith(MockitoJUnitRunner.class)
public class SlimmeMeterControllerTest {

    @InjectMocks
    private SlimmeMeterController slimmeMeterController;

    @Mock
    private OpgenomenVermogenService opgenomenVermogenService;
    @Mock
    private MeterstandService meterstandService;

    @Captor
    private ArgumentCaptor<Meterstand> meterstandCaptor;
    @Captor
    private ArgumentCaptor<OpgenomenVermogen> opgenomenVermogenCaptor;

    @Test
    public void whenSaveThenMeterstandAndOpgenomenVermogenSaved() {
        Dsmr42Reading dsmr42Reading = new Dsmr42Reading();
        LocalDateTime dateTime = LocalDate.of(2016, Month.NOVEMBER, 12).atTime(14, 18);
        dsmr42Reading.setDatumtijd(DateTimeUtil.toMillisSinceEpoch(dateTime));
        StroomTariefIndicator stroomTariefIndicator = StroomTariefIndicator.NORMAAL;
        dsmr42Reading.setStroomTariefIndicator((int) stroomTariefIndicator.getId());
        dsmr42Reading.setGas(new BigDecimal("201.876234"));
        dsmr42Reading.setStroomTarief1(new BigDecimal("352.907511"));
        dsmr42Reading.setStroomTarief2(new BigDecimal("2341.234345"));
        dsmr42Reading.setStroomOpgenomenVermogenInWatt(424);

        slimmeMeterController.save(dsmr42Reading);

        verify(meterstandService).save(meterstandCaptor.capture());
        assertThat(meterstandCaptor.getValue().getDateTime()).isEqualTo(dateTime);
        assertThat(meterstandCaptor.getValue().getGas()).isEqualTo(new BigDecimal("201.876"));
        assertThat(meterstandCaptor.getValue().getStroomTariefIndicator()).isEqualTo(stroomTariefIndicator);
        assertThat(meterstandCaptor.getValue().getStroomTarief1()).isEqualTo(new BigDecimal("352.908"));
        assertThat(meterstandCaptor.getValue().getStroomTarief2()).isEqualTo(new BigDecimal("2341.234"));

        verify(opgenomenVermogenService).save(opgenomenVermogenCaptor.capture());
        assertThat(opgenomenVermogenCaptor.getValue().getDatumtijd()).isEqualTo(dateTime);
        assertThat(opgenomenVermogenCaptor.getValue().getWatt()).isEqualTo(dsmr42Reading.getStroomOpgenomenVermogenInWatt());
        assertThat(opgenomenVermogenCaptor.getValue().getTariefIndicator()).isEqualTo(stroomTariefIndicator);
    }
}