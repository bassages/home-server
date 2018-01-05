package nl.homeserver.util;

import static org.mockito.Mockito.doReturn;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeMachine {

    public static void timeTravelTo(Clock mockedClock, LocalDateTime localDateTime) {
        Clock fixedClock = Clock.fixed(localDateTime.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        useClock(mockedClock, fixedClock);
    }

    public static void useSystemDefaultClock(Clock mockedClock) {
        useClock(mockedClock, Clock.systemDefaultZone());
    }

    private static void useClock(Clock mockedClock, Clock realClock) {
        doReturn(realClock.instant()).when(mockedClock).instant();
        doReturn(realClock.getZone()).when(mockedClock).getZone();
        doReturn(realClock.millis()).when(mockedClock).millis();
    }

}
