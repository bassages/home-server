package nl.homeserver.housekeeping;

public final class HousekeepingSchedule {
    private static final String ONE_AM = "0 0 1 * * *";
    private static final String TWO_AM = "0 0 2 * * *";

    public static final String OPGENOMEN_VERMOGEN = ONE_AM;
    public static final String METERSTAND = TWO_AM;
}
