package nl.wiegman.homecontrol.services.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Throwable> {

    static final String ERROR_CODE_UNIQUE_KEY_CONSTRAINT_VIOLATION = "UNIQUE_KEY_CONSTRAINT_VIOLATION";

    public Response toResponse(Throwable throwable) {

        if (throwable instanceof DataIntegrityViolationException
                && throwable.getCause() instanceof ConstraintViolationException
                && (((ConstraintViolationException) throwable.getCause()).getConstraintName().startsWith("UK_"))) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(ERROR_CODE_UNIQUE_KEY_CONSTRAINT_VIOLATION, ExceptionUtils.getStackTrace(throwable)))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(throwable))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
