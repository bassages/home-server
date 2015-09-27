package nl.wiegman.homecontrol.services.realtime;

import nl.wiegman.homecontrol.services.apimodel.OpgenomenVermogen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;

@Controller
public class RealTimeOpgenomenVermogenController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private TaskScheduler scheduler = new ConcurrentTaskScheduler();

    @PostConstruct
    private void broadcastPeriodically() {
        scheduler.scheduleAtFixedRate(() -> messagingTemplate.convertAndSend("/topic/elektriciteit/opgenomenVermogen", getRandomOpgenomenVermogen()), 1000);
    }

    private OpgenomenVermogen getRandomOpgenomenVermogen() {
        OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setDatumtijd(System.currentTimeMillis());
        opgenomenVermogen.setOpgenomenVermogenInWatt((int)(Math.random() * (2000 - 50)) + 50);
        return opgenomenVermogen;
    }

}