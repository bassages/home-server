package nl.wiegman.homecontrol.services.config.converters;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * To be able to recieve datetime as a JAX-RS param
 */
@Provider
public class LocalDateTimeParamConverterProvider implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> type, Type genericType, Annotation[] annotations) {
        ParamConverter<T> result = null;
        if (type.equals(LocalDateTime.class)) {
            result = (ParamConverter<T>) new DateTimeParamConverter();
        }
        return result;
    }

    private static class DateTimeParamConverter implements ParamConverter<LocalDateTime> {
        @Override
        public LocalDateTime fromString(String value) {
            return LocalDateTime.parse(value);
        }

        @Override
        public String toString(LocalDateTime value) {
            return value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
