package nl.wiegman.home.api;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler {
    static final String ERROR_CODE_UNIQUE_KEY_CONSTRAINT_VIOLATION = "UNIQUE_KEY_CONSTRAINT_VIOLATION";

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> onDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return new ResponseEntity<>(new ErrorResponse(ERROR_CODE_UNIQUE_KEY_CONSTRAINT_VIOLATION, ExceptionUtils.getStackTrace(ex)), HttpStatus.BAD_REQUEST);
    }
}
