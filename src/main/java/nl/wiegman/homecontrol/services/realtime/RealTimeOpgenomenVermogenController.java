package nl.wiegman.homecontrol.services.realtime;

import nl.wiegman.homecontrol.services.apimodel.OpgenomenVermogen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class RealTimeOpgenomenVermogenController {

    public static final String TOPIC_ELEKTRICITEIT_OPGENOMEN_VERMOGEN = "/topic/elektriciteit/opgenomenVermogen";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onApplicationEvent(OpgenomenVermogen opgenomenVermogen) {
        messagingTemplate.convertAndSend(TOPIC_ELEKTRICITEIT_OPGENOMEN_VERMOGEN, opgenomenVermogen);
    }
}