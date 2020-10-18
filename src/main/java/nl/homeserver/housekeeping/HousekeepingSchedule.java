package nl.homeserver.housekeeping;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HousekeepingSchedule {
    private static final String FIVE_MINUTES_PAST_MIDNIGHT = "0 5 0 * * *";
    private static final String ONE_AM = "0 0 1 * * *";
    private static final String TWO_AM = "0 0 2 * * *";

    public static final String WARMUP_CACHE = FIVE_MINUTES_PAST_MIDNIGHT;
    public static final String OPGENOMEN_VERMOGEN_CLEANUP = ONE_AM;
    public static final String METERSTAND_CLEANUP = TWO_AM;
}
