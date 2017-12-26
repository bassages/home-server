package nl.wiegman.home.energie;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.text.ParseException;
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
public class MeterstandServiceTest {

    @Mock
    private CacheService cacheService;
    @Mock
    private MeterstandRepository meterstandRepository;

    @InjectMocks
    private MeterstandService meterstandService;

    @Captor
    private ArgumentCaptor<List<Meterstand>> deletedMeterstandCaptor;

    @Test
    public void shouldClearCacheOnCleanup() {
        meterstandService.cleanup(mock(Date.class));
        verify(cacheService).clear(VerbruikServiceCached.CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE);
        verify(cacheService).clear(VerbruikServiceCached.CACHE_NAME_GAS_VERBRUIK_IN_PERIODE);
    }

    @Test
    public void shouldCleanupOneDay() throws Exception {
        Date date = toDate("14:46:30");

        ArgumentCaptor<Long> fromDateCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> toDateCaptor = ArgumentCaptor.forClass(Long.class);
        when(meterstandRepository.findByDatumtijdBetween(fromDateCaptor.capture(), toDateCaptor.capture())).thenReturn(emptyList());

        meterstandService.cleanup(date);

        assertThat(new Date(fromDateCaptor.getValue())).withDateFormat("yyyy-MM-dd HH:mm:ss.SSS").isEqualTo("2016-01-01 00:00:00.000");
        assertThat(new Date(toDateCaptor.getValue())).withDateFormat("yyyy-MM-dd HH:mm:ss.SSS").isEqualTo("2016-01-01 23:59:59.999");

        verify(meterstandRepository).findByDatumtijdBetween(anyLong(), anyLong());
        verifyNoMoreInteractions(meterstandRepository);
    }

    @Test
    public void shouldKeepFirstAndLastInOneHour() throws Exception {
        Date date = toDate("14:46:30");

        Meterstand m1 = new Meterstand();
        m1.setDatumtijd(toDate("12:00:00").getTime());
        Meterstand m2 = new Meterstand();
        m2.setDatumtijd(toDate("12:15:00").getTime());
        Meterstand m3 = new Meterstand();
        m3.setDatumtijd(toDate("12:30:00").getTime());
        Meterstand m4 = new Meterstand();
        m4.setDatumtijd(toDate("12:45:00").getTime());
        Meterstand m5 = new Meterstand();
        m5.setDatumtijd(toDate("12:59:00").getTime());

        when(meterstandRepository.findByDatumtijdBetween(anyLong(), anyLong())).thenReturn(asList(m1, m2, m3, m4, m5));

        meterstandService.cleanup(date);

        verify(meterstandRepository).deleteInBatch(deletedMeterstandCaptor.capture());

        assertThat(deletedMeterstandCaptor.getValue()).containsExactlyInAnyOrder(m2, m3, m4);
    }

    @Test
    public void shouldKeepFirstAndLastInOneHourWhenOnlyOneMeterstandExists() throws Exception {
        Date date = toDate("14:46:30");

        Meterstand m1 = new Meterstand();
        m1.setDatumtijd(toDate("12:00:00").getTime());

        when(meterstandRepository.findByDatumtijdBetween(anyLong(), anyLong())).thenReturn(singletonList(m1));

        meterstandService.cleanup(date);

        verify(meterstandRepository).findByDatumtijdBetween(anyLong(), anyLong());
        verifyNoMoreInteractions(meterstandRepository);
    }

    @Test
    public void shouldDeletePerHour() throws Exception {
        Date date = toDate("14:46:30");

        Meterstand m1 = new Meterstand();
        m1.setDatumtijd(toDate("12:00:00").getTime());
        Meterstand m2 = new Meterstand();
        m2.setDatumtijd(toDate("12:15:00").getTime());
        Meterstand m3 = new Meterstand();
        m3.setDatumtijd(toDate("12:30:00").getTime());

        Meterstand m4 = new Meterstand();
        m4.setDatumtijd(toDate("13:00:00").getTime());
        Meterstand m5 = new Meterstand();
        m5.setDatumtijd(toDate("13:15:00").getTime());
        Meterstand m6 = new Meterstand();
        m6.setDatumtijd(toDate("13:30:00").getTime());

        when(meterstandRepository.findByDatumtijdBetween(anyLong(), anyLong())).thenReturn(asList(m1, m2, m3, m4, m5, m6));

        meterstandService.cleanup(date);

        verify(meterstandRepository, times(2)).deleteInBatch(deletedMeterstandCaptor.capture());

        assertThat(deletedMeterstandCaptor.getAllValues().get(0)).containsOnly(m2);
        assertThat(deletedMeterstandCaptor.getAllValues().get(1)).containsOnly(m5);
    }

    private Date toDate(String timeString) throws ParseException {
        return DateUtils.parseDate("2016-01-01 " + timeString, "yyyy-MM-dd HH:mm:ss");
    }

}