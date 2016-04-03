package nl.wiegman.home.realtime;

import nl.wiegman.home.model.Klimaat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class RealTimeTemperatuurController {

    public static final String TOPIC = "/topic/klimaat";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onApplicationEvent(UpdateEvent event) {
        Object updatedObject = event.getUpdatedObject();
        if (updatedObject instanceof Klimaat) {
            messagingTemplate.convertAndSend(TOPIC, updatedObject);
        }
    }
}