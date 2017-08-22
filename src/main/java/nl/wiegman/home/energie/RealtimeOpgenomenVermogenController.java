package nl.wiegman.home.energie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import nl.wiegman.home.UpdateEvent;

@Controller
public class RealtimeOpgenomenVermogenController {

    public static final String TOPIC = "/topic/opgenomen-vermogen";

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public RealtimeOpgenomenVermogenController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onApplicationEvent(UpdateEvent event) {
        Object updatedObject = event.getUpdatedObject();
        if (updatedObject instanceof OpgenomenVermogen) {
            messagingTemplate.convertAndSend(TOPIC, updatedObject);
        }
    }
}