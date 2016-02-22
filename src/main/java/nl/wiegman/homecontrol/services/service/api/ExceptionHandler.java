package nl.wiegman.homecontrol.services.service.api;

import nl.wiegman.homecontrol.services.service.ErrorResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionHandler implements javax.ws.rs.ext.ExceptionMapper<Throwable> {

    private final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    static final String ERROR_CODE_UNIQUE_KEY_CONSTRAINT_VIOLATION = "UNIQUE_KEY_CONSTRAINT_VIOLATION";

    public Response toResponse(Throwable throwable) {
        Response response = null;

        if (throwable instanceof WebApplicationException) {
            response = ((WebApplicationException) throwable).getResponse();
        } else {
            logger.error("Failed to complete request", throwable);

            if (throwable instanceof DataIntegrityViolationException
                    && throwable.getCause() instanceof ConstraintViolationException
                    && (((ConstraintViolationException) throwable.getCause()).getConstraintName().startsWith("UK_"))) {

                response = Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(ERROR_CODE_UNIQUE_KEY_CONSTRAINT_VIOLATION, ExceptionUtils.getStackTrace(throwable)))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .build();
            } else {
                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ErrorResponse(throwable))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .build();
            }
        }
        return response;
    }
}
