package nl.homeserver.climate;

enum SensorType {
    TEMPERATUUR,
    LUCHTVOCHTIGHEID;

    static SensorType toSensorType(final String string) {
        return SensorType.valueOf(string.toUpperCase());
    }
}
