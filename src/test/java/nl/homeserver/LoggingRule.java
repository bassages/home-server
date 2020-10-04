package nl.homeserver;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.ArgumentCaptor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;

public class LoggingRule implements TestRule {

    private final LoggerEventCaptor loggerEventCaptor;

    public LoggingRule(final Class<?> loggerClass) {
        loggerEventCaptor = new LoggerEventCaptor(loggerClass);
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return loggerEventCaptor.apply(base, description);
    }

    public void setLevel(final Level level) {
        loggerEventCaptor.setLevel(level);
    }

    public ArgumentCaptor<LoggingEvent> getLoggedEventCaptor() {
        return loggerEventCaptor.getLoggedEventCaptor();
    }
}
