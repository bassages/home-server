package nl.homeserver;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateTimeUtil {

    private DateTimeUtil() {
        // Hide public constructor
    }

    public static long toMillisSinceEpoch(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime toLocalDateTime(long millisSinceEpoch) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millisSinceEpoch), ZoneId.systemDefault());
    }
}
