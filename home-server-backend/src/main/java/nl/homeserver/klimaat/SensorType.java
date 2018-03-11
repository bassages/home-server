package nl.homeserver.klimaat;

public enum SensorType {
    TEMPERATUUR,
    LUCHTVOCHTIGHEID;

    public static SensorType toSensorType(String string) {
        return SensorType.valueOf(string.toUpperCase());
    }
}
