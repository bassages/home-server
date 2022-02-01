package nl.homeserver;

import ch.qos.logback.classic.Level;
import org.assertj.core.api.Condition;

import ch.qos.logback.classic.spi.LoggingEvent;

public class ContainsMessageAtLevel extends Condition<LoggingEvent> {
    private final Level expectedLevel;
    private final String expectedMessage;

    public ContainsMessageAtLevel(final String expectedMessage, final Level expectedLevel) {
        super("Contains \"" + expectedMessage + "\" at level " + expectedLevel);
        this.expectedMessage = expectedMessage;
        this.expectedLevel = expectedLevel;
    }

    @Override
    public boolean matches(final LoggingEvent loggingEvent) {
        return loggingEvent.getFormattedMessage().contains(expectedMessage) && loggingEvent.getLevel() == expectedLevel;
    }
}
