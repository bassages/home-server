package nl.wiegman.home.energie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import nl.wiegman.home.UpdateEvent;

@Controller
public class RealtimeMeterstandController {

    public static final String TOPIC = "/topic/meterstand";

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public RealtimeMeterstandController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onApplicationEvent(UpdateEvent event) {
        Object updatedObject = event.getUpdatedObject();
        if (updatedObject instanceof Meterstand) {
            messagingTemplate.convertAndSend(TOPIC, updatedObject);
        }
    }
}