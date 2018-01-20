package nl.homeserver;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler {
    private static final String ERROR_CODE_UNIQUE_KEY_CONSTRAINT_VIOLATION = "UNIQUE_KEY_CONSTRAINT_VIOLATION";
    private static final String ERROR_CODE_NOT_FOUND = "NOT_FOUND";

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> onDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return new ResponseEntity<>(new ErrorResponse(ERROR_CODE_UNIQUE_KEY_CONSTRAINT_VIOLATION, getStackTrace(ex)), BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> requestHandlingNoHandlerFound(ResourceNotFoundException resourceNotFoundException) {
        return new ResponseEntity<>(new ErrorResponse(ERROR_CODE_NOT_FOUND, resourceNotFoundException.getMessage()), HttpStatus.NOT_FOUND);
    }
}
