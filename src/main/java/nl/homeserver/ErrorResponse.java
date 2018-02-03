package nl.homeserver;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.Getter;
import lombok.Setter;

public class ErrorResponse {

    @Getter @Setter
    private String code;
    @Getter @Setter
    private String details;

    public ErrorResponse(Throwable t) {
        code = t.getClass().getName();
        details = ExceptionUtils.getStackTrace(t);
    }

    public ErrorResponse(String code, String details) {
        this.code = code;
        this.details = details;
    }
}
