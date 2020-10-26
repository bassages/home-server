package nl.homeserver;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(LogCaptorExtension.class)
public @interface CaptureLogging {
    Class<?> value();
}
