package nl.wiegman.home.realtime;

import nl.wiegman.home.model.Meterstand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class RealTimeMeterstandController {

    public static final String TOPIC = "/topic/meterstand";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onApplicationEvent(UpdateEvent event) {
        Object updatedObject = event.getUpdatedObject();
        if (updatedObject instanceof Meterstand) {
            messagingTemplate.convertAndSend(TOPIC, updatedObject);
        }

    }
}