package nl.wiegman.home.energie;

import nl.wiegman.home.UpdateEvent;
import nl.wiegman.home.energie.Meterstand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class RealtimeMeterstandController {

    public static final String TOPIC = "/topic/meterstand";

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onApplicationEvent(UpdateEvent event) {
        Object updatedObject = event.getUpdatedObject();
        if (updatedObject instanceof Meterstand) {
            messagingTemplate.convertAndSend(TOPIC, updatedObject);
        }
    }
}