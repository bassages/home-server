package nl.homeserver.util;

import static org.mockito.Mockito.doReturn;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class TimeMachine {

    public static void timeTravelTo(final Clock mockedClock, final LocalDateTime localDateTime) {
        final Clock fixedClock = Clock.fixed(localDateTime.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        useClock(mockedClock, fixedClock);
    }

    public static void useSystemDefaultClock(final Clock mockedClock) {
        useClock(mockedClock, Clock.systemDefaultZone());
    }

    private static void useClock(final Clock mockedClock, final Clock realClock) {
        doReturn(realClock.instant()).when(mockedClock).instant();
        doReturn(realClock.getZone()).when(mockedClock).getZone();
    }
}
