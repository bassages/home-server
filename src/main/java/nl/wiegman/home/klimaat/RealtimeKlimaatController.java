package nl.wiegman.home.klimaat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import nl.wiegman.home.UpdateEvent;

@Controller
public class RealtimeKlimaatController {
    private static final String TOPIC = "/topic/klimaat";

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public RealtimeKlimaatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onApplicationEvent(UpdateEvent event) {
        Object updatedObject = event.getUpdatedObject();
        if (updatedObject instanceof Klimaat) {
            messagingTemplate.convertAndSend(TOPIC, updatedObject);
        }
    }
}