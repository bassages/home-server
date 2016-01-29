package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Kosten;
import nl.wiegman.homecontrol.services.repository.KostenRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KostenRestTest {

    @Mock
    KostenRepository kostenRepositoryMock;

    @InjectMocks
    KostenRest kostenRest;

    @Test
    public void recalculateTotEnMetWithChanges() {
        Kosten k1 = new Kosten();
        k1.setVan(4l);

        Kosten k2 = new Kosten();
        k2.setVan(7l);

        Kosten k3 = new Kosten();
        k3.setVan(30l);

        Kosten k4 = new Kosten();
        k4.setVan(345l);

        when(kostenRepositoryMock.findAll(any(Sort.class))).thenReturn(Arrays.asList(k1, k2, k3, k4));

        kostenRest.recalculateTotEnMet();

        assertThat(k1.getTotEnMet(), is(equalTo(6l)));
        assertThat(k2.getTotEnMet(), is(equalTo(29l)));
        assertThat(k3.getTotEnMet(), is(equalTo(344l)));
        assertThat(k4.getTotEnMet(), is(equalTo(KostenRest.SINT_JUTTEMIS)));

        verify(kostenRepositoryMock, times(4)).save(any(Kosten.class));
    }

    @Test
    public void recalculateTotEnMetWithoutChanges() {
        Kosten k1 = new Kosten();
        k1.setVan(4l);
        k1.setTotEnMet(6l);

        Kosten k2 = new Kosten();
        k2.setVan(7l);
        k2.setTotEnMet(29l);

        Kosten k3 = new Kosten();
        k3.setVan(30l);
        k3.setTotEnMet(344l);

        Kosten k4 = new Kosten();
        k4.setVan(345l);
        k4.setTotEnMet(KostenRest.SINT_JUTTEMIS);

        when(kostenRepositoryMock.findAll(any(Sort.class))).thenReturn(Arrays.asList(k1, k2, k3, k4));

        kostenRest.recalculateTotEnMet();

        assertThat(k1.getTotEnMet(), is(equalTo(6l)));
        assertThat(k2.getTotEnMet(), is(equalTo(29l)));
        assertThat(k3.getTotEnMet(), is(equalTo(344l)));
        assertThat(k4.getTotEnMet(), is(equalTo(KostenRest.SINT_JUTTEMIS)));

        verify(kostenRepositoryMock, never()).save(any(Kosten.class));
    }

}
