package nl.homeserver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class ControllerExceptionHandlerTest {

    @Test
    public void givenDataIntegrityViolationExceptionWhenCreateResponseThenErrorResponseCreated() {
        final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();

        final String message = "FUBAR";
        final DataIntegrityViolationException exception = new DataIntegrityViolationException(message);

        final ResponseEntity<ErrorResponse> errorResponseResponseEntity = controllerExceptionHandler.createResponse(exception);

        assertThat(errorResponseResponseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorResponseResponseEntity.getBody()).isNotNull();
        assertThat(errorResponseResponseEntity.getBody().getCode()).isEqualTo(ControllerExceptionHandler.ERROR_CODE_UNIQUE_KEY_CONSTRAINT_VIOLATION);
        assertThat(errorResponseResponseEntity.getBody().getDetails()).contains(message);
    }

    @Test
    public void givenResourceNotFoundExceptionWhenCreateResponseThenErrorResponseCreated() {
        final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();

        final String message = "FUBAR";
        final ResourceNotFoundException exception = new ResourceNotFoundException(message, "someNotNullResourceId");

        final ResponseEntity<ErrorResponse> errorResponseResponseEntity = controllerExceptionHandler.createResponse(exception);

        assertThat(errorResponseResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(errorResponseResponseEntity.getBody()).isNotNull();
        assertThat(errorResponseResponseEntity.getBody().getCode()).isEqualTo(ControllerExceptionHandler.ERROR_CODE_NOT_FOUND);
        assertThat(errorResponseResponseEntity.getBody().getDetails()).contains(message);
    }
}