package nl.wiegman.home.klimaat;

public enum SensorType {
    TEMPERATUUR,
    LUCHTVOCHTIGHEID;

    public static SensorType fromString(String string) {
        return SensorType.valueOf(string.toUpperCase());
    }
}
