package nl.homeserver;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.rules.ExternalResource;
import org.mockito.ArgumentCaptor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;

@SuppressWarnings("unchecked")
public class LoggerEventCaptor extends ExternalResource {

    private final Logger logger;
    private final Appender mockAppender;

    private ArgumentCaptor<LoggingEvent> loggingEventCaptor;

    LoggerEventCaptor(final Class<?> loggerClass) {
        logger = (Logger) getLogger(loggerClass);
        mockAppender = mock(Appender.class);
    }

    @Override
    protected void before() {
        loggingEventCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
        doAnswer(invocation -> null).when(mockAppender).doAppend(loggingEventCaptor.capture());
        logger.addAppender(mockAppender);
    }

    @Override
    protected void after() {
        logger.detachAppender(mockAppender);
    }

    void setLevel(final Level level) {
        logger.setLevel(level);
    }

    ArgumentCaptor<LoggingEvent> getLoggedEventCaptor() {
        return loggingEventCaptor;
    }
}
