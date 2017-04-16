package nl.wiegman.home.api.dto;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class ErrorResponse {

    private String code;
    private String details;

    public ErrorResponse(Throwable t) {
        code = t.getClass().getName();
        details = ExceptionUtils.getStackTrace(t);
    }

    public ErrorResponse(String code, String details) {
        this.code = code;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
