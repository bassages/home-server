package nl.homeserver;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.extension.*;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.slf4j.LoggerFactory.getLogger;

public class LogCaptorExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, ParameterResolver {
    private Logger logger;
    private Appender mockAppender;
    private ArgumentCaptor<LoggingEvent> loggingEventCaptor;

    @Override
    public void beforeTestExecution(final ExtensionContext context) {
        final CaptureLogging annotation = context.getElement()
                                                 .map(annotatedElement -> annotatedElement.getAnnotation(CaptureLogging.class))
                                                 .orElseThrow(() -> new RuntimeException("Unable to find annotation"));
        logger = (Logger) getLogger(annotation.value());
        logger.setLevel(Level.ALL);
        mockAppender = mock(Appender.class);
        loggingEventCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
        doAnswer(invocation -> null).when(mockAppender).doAppend(loggingEventCaptor.capture());
        logger.addAppender(mockAppender);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        logger.detachAppender(mockAppender);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == ArgumentCaptor.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        return loggingEventCaptor;
    }
}
