package nl.wiegman.home.energie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.wiegman.home.cache.CacheService;

@RunWith(MockitoJUnitRunner.class)
public class OpgenomenVermogenServiceTest {

    @Mock
    private OpgenomenVermogenRepository opgenomenVermogenRepository;
    @Mock
    private CacheService cacheService;

    @InjectMocks
    private OpgenomenVermogenService opgenomenVermogenService;

    @Captor
    private ArgumentCaptor<List<OpgenomenVermogen>> deletedOpgenomenVermogenCaptor;

    @Test
    public void shouldCleanupOneDay() throws Exception {
        Date date = toDate("14:46:30");

        ArgumentCaptor<Date> fromDateCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> toDateCaptor = ArgumentCaptor.forClass(Date.class);
        when(opgenomenVermogenRepository.getOpgenomenVermogen(fromDateCaptor.capture(), toDateCaptor.capture())).thenReturn(Collections.EMPTY_LIST);

        opgenomenVermogenService.cleanup(date);

        assertThat(fromDateCaptor.getValue()).withDateFormat("yyyy-MM-dd HH:mm:ss.SSS").isEqualTo("2016-01-01 00:00:00.000");
        assertThat(toDateCaptor.getValue()).withDateFormat("yyyy-MM-dd HH:mm:ss.SSS").isEqualTo("2016-01-02 00:00:00.000");

        verify(opgenomenVermogenRepository).getOpgenomenVermogen(any(), any());
        verifyNoMoreInteractions(opgenomenVermogenRepository);
    }

    @Test
    public void shouldKeepLatestRecordInMinuteWhenWattIsTheSame() throws Exception {
        Date date = toDate("14:46:30");

        OpgenomenVermogen ov1 = new OpgenomenVermogen();
        ov1.setDatumtijd(toDate("12:00:00"));
        ov1.setWatt(1);
        OpgenomenVermogen ov2 = new OpgenomenVermogen();
        ov2.setDatumtijd(toDate("12:00:10"));
        ov2.setWatt(1);
        OpgenomenVermogen ov3 = new OpgenomenVermogen();
        ov3.setDatumtijd(toDate("12:00:20"));
        ov3.setWatt(1);

        when(opgenomenVermogenRepository.getOpgenomenVermogen(any(), any())).thenReturn(Arrays.asList(ov1, ov2, ov3));

        opgenomenVermogenService.cleanup(date);

        verify(opgenomenVermogenRepository).deleteInBatch(deletedOpgenomenVermogenCaptor.capture());

        assertThat(deletedOpgenomenVermogenCaptor.getValue()).containsExactlyInAnyOrder(ov1, ov2);
    }

    @Test
    public void shouldKeepHighestWatt() throws Exception {
        Date date = toDate("14:46:30");

        OpgenomenVermogen ov1 = new OpgenomenVermogen();
        ov1.setDatumtijd(toDate("12:00:00"));
        ov1.setWatt(3);
        OpgenomenVermogen ov2 = new OpgenomenVermogen();
        ov2.setDatumtijd(toDate("12:00:10"));
        ov2.setWatt(2);
        OpgenomenVermogen ov3 = new OpgenomenVermogen();
        ov3.setDatumtijd(toDate("12:00:20"));
        ov3.setWatt(1);

        when(opgenomenVermogenRepository.getOpgenomenVermogen(any(), any())).thenReturn(Arrays.asList(ov1, ov2, ov3));

        opgenomenVermogenService.cleanup(date);

        verify(opgenomenVermogenRepository).deleteInBatch(deletedOpgenomenVermogenCaptor.capture());

        assertThat(deletedOpgenomenVermogenCaptor.getValue()).containsExactlyInAnyOrder(ov2, ov3);
    }

    @Test
    public void shouldCleanUpPerMinuteAndDeletePerHour() throws Exception {
        Date date = toDate("14:46:30");

        OpgenomenVermogen ov1 = new OpgenomenVermogen();
        ov1.setDatumtijd(toDate("12:00:00"));
        OpgenomenVermogen ov2 = new OpgenomenVermogen();
        ov2.setDatumtijd(toDate("12:00:10"));

        OpgenomenVermogen ov3 = new OpgenomenVermogen();
        ov3.setDatumtijd(toDate("12:01:00"));
        OpgenomenVermogen ov4 = new OpgenomenVermogen();
        ov4.setDatumtijd(toDate("12:01:10"));

        OpgenomenVermogen ov5 = new OpgenomenVermogen();
        ov5.setDatumtijd(toDate("13:01:00"));
        OpgenomenVermogen ov6 = new OpgenomenVermogen();
        ov6.setDatumtijd(toDate("13:01:10"));

        when(opgenomenVermogenRepository.getOpgenomenVermogen(any(), any())).thenReturn(Arrays.asList(ov1, ov2, ov3, ov4, ov5, ov6));

        opgenomenVermogenService.cleanup(date);

        verify(opgenomenVermogenRepository, times(2)).deleteInBatch(deletedOpgenomenVermogenCaptor.capture());

        assertThat(deletedOpgenomenVermogenCaptor.getAllValues().get(0)).containsExactlyInAnyOrder(ov1, ov3);
        assertThat(deletedOpgenomenVermogenCaptor.getAllValues().get(1)).containsExactlyInAnyOrder(ov5);
    }

    private Date toDate(String timeString) throws ParseException {
        return DateUtils.parseDate("2016-01-01 " + timeString, "yyyy-MM-dd HH:mm:ss");
    }

}