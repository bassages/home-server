package nl.wiegman.home.util;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeMachine {

    public static Clock timeTravelTo(LocalDateTime localDateTime) {
        return Clock.fixed(localDateTime.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    }
}
