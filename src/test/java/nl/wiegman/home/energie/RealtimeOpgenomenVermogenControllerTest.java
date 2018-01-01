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
public class RealtimeOpgenomenVermogenControllerTest {

    @InjectMocks
    private RealtimeOpgenomenVermogenController realtimeOpgenomenVermogenController;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    private UpdateEvent updateEvent;

    @Test
    public void givenOpgenomenVermogenWhenApplicationEentThenConvertedAndSend() {
        OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        when(updateEvent.getUpdatedObject()).thenReturn(opgenomenVermogen);

        realtimeOpgenomenVermogenController.onApplicationEvent(updateEvent);

        verify(simpMessagingTemplate).convertAndSend(RealtimeOpgenomenVermogenController.TOPIC, opgenomenVermogen);
    }

    @Test
    public void givenOtherTypeOfObjectWhenApplicationEentThenConvertedAndSend() {
        Object someObject = new Object();
        when(updateEvent.getUpdatedObject()).thenReturn(someObject);

        realtimeOpgenomenVermogenController.onApplicationEvent(updateEvent);

        verifyZeroInteractions(simpMessagingTemplate);
    }
}