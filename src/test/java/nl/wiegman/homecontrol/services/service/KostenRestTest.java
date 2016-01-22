package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Kosten;
import nl.wiegman.homecontrol.services.repository.KostenRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class KostenRestTest {

    @Mock
    KostenRepository kostenRepositoryMock;

    @InjectMocks
    KostenRest kostenRest;

    @Test
    public void recalculateTotEnMetWithChanges() {
        Kosten k1 = new Kosten();
        k1.setVan(4);

        Kosten k2 = new Kosten();
        k2.setVan(7);

        Kosten k3 = new Kosten();
        k3.setVan(30);

        Kosten k4 = new Kosten();
        k4.setVan(345);

        when(kostenRepositoryMock.findAll(any(Sort.class))).thenReturn(Arrays.asList(new Kosten[] {k1, k2, k3, k4}));

        kostenRest.recalculateTotEnMet();

        assertThat(k1.getTotEnMet(), is(equalTo(6l)));
        assertThat(k2.getTotEnMet(), is(equalTo(29l)));
        assertThat(k3.getTotEnMet(), is(equalTo(344l)));
        assertThat(k4.getTotEnMet(), is(equalTo(Long.MAX_VALUE)));

        verify(kostenRepositoryMock, times(4)).save(any(Kosten.class));
    }

    @Test
    public void recalculateTotEnMetWithoutChanges() {
        Kosten k1 = new Kosten();
        k1.setVan(4);
        k1.setTotEnMet(6);

        Kosten k2 = new Kosten();
        k2.setVan(7);
        k2.setTotEnMet(29);

        Kosten k3 = new Kosten();
        k3.setVan(30);
        k3.setTotEnMet(344);

        Kosten k4 = new Kosten();
        k4.setVan(345);
        k4.setTotEnMet(Long.MAX_VALUE);

        when(kostenRepositoryMock.findAll(any(Sort.class))).thenReturn(Arrays.asList(new Kosten[] {k1, k2, k3, k4}));

        kostenRest.recalculateTotEnMet();

        assertThat(k1.getTotEnMet(), is(equalTo(6l)));
        assertThat(k2.getTotEnMet(), is(equalTo(29l)));
        assertThat(k3.getTotEnMet(), is(equalTo(344l)));
        assertThat(k4.getTotEnMet(), is(equalTo(Long.MAX_VALUE)));

        verify(kostenRepositoryMock, never()).save(any(Kosten.class));
    }

}
