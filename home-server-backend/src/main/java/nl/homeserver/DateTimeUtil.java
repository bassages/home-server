package nl.homeserver;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateTimeUtil {

    private DateTimeUtil() {
        // Hide public constructor
    }

    public static long toMillisSinceEpoch(final LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime();
    }
}
