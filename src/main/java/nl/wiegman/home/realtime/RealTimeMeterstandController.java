package nl.wiegman.home.realtime;

import nl.wiegman.home.model.event.UpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class RealTimeMeterstandController {

    public static final String TOPIC_METERSTAND = "/topic/meterstand";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onApplicationEvent(UpdateEvent event) {
        messagingTemplate.convertAndSend(TOPIC_METERSTAND, event.getUpdatedObject());
    }
}