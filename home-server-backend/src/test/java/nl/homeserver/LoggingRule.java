package nl.homeserver;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.ArgumentCaptor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;

public class LoggingRule implements TestRule {

    private final Logger logger;
    private final Appender mockAppender;

    private ArgumentCaptor<LoggingEvent> loggingEventCaptor;

    public LoggingRule(Class<?> loggerClass) {
        this.logger = (Logger) getLogger(loggerClass);
        mockAppender = mock(Appender.class);
    }

    private final SetUpAndTearDown setUpAndTearDown = new SetUpAndTearDown();

    @SuppressWarnings("unchecked")
    private class SetUpAndTearDown extends ExternalResource {
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
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return setUpAndTearDown.apply(base, description);
    }

    public void setLevel(Level level) {
        logger.setLevel(level);
    }

    public ArgumentCaptor<LoggingEvent> getLoggedEventCaptor() {
        return loggingEventCaptor;
    }

}
