package nl.wiegman.home.energie;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import nl.wiegman.home.UpdateEvent;

@RunWith(MockitoJUnitRunner.class)
public class RealtimeMeterstandControllerTest {

    @InjectMocks
    private RealtimeMeterstandController realtimeORealtimeMeterstandController;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    private UpdateEvent updateEvent;

    @Test
    public void givenMeterstandWhenApplicationEentThenConvertedAndSend() {
        Meterstand meterstand = new Meterstand();
        when(updateEvent.getUpdatedObject()).thenReturn(meterstand);

        realtimeORealtimeMeterstandController.onApplicationEvent(updateEvent);

        verify(simpMessagingTemplate).convertAndSend(RealtimeMeterstandController.TOPIC, meterstand);
    }

    @Test
    public void givenOtherTypeOfObjectWhenApplicationEentThenConvertedAndSend() {
        Object someObject = new Object();
        when(updateEvent.getUpdatedObject()).thenReturn(someObject);

        realtimeORealtimeMeterstandController.onApplicationEvent(updateEvent);

        verifyZeroInteractions(simpMessagingTemplate);
    }
}