package nl.wiegman.home.klimaat;

import nl.wiegman.home.UpdateEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class RealtimeKlimaatController {

    public static final String TOPIC = "/topic/klimaat";

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onApplicationEvent(UpdateEvent event) {
        Object updatedObject = event.getUpdatedObject();
        if (updatedObject instanceof Klimaat) {
            messagingTemplate.convertAndSend(TOPIC, updatedObject);
        }
    }
}