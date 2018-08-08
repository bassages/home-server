package nl.homeserver;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class ControllerExceptionHandler {
    protected static final String ERROR_CODE_UNIQUE_KEY_CONSTRAINT_VIOLATION = "UNIQUE_KEY_CONSTRAINT_VIOLATION";
    protected static final String ERROR_CODE_NOT_FOUND = "NOT_FOUND";

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> createResponse(final DataIntegrityViolationException dataIntegrityViolationException) {
        return new ResponseEntity<>(new ErrorResponse(ERROR_CODE_UNIQUE_KEY_CONSTRAINT_VIOLATION, getStackTrace(dataIntegrityViolationException)), BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> createResponse(final ResourceNotFoundException resourceNotFoundException) {
        return new ResponseEntity<>(new ErrorResponse(ERROR_CODE_NOT_FOUND, resourceNotFoundException.getMessage()), NOT_FOUND);
    }
}
