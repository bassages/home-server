package nl.wiegman.homecontrol.services.service.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class CurrentDateTime implements HealthIndicator {

    @Override
    public Health health() {
        Date now = new Date();
        return Health.up()
                .withDetail("The date/time on the server is", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.sss").format(now))
                .withDetail("The epoch timestamp on the server is", now.getTime())
                .build();
    }

}
