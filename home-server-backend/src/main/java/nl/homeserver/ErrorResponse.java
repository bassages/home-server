package nl.homeserver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorResponse {

    @Getter
    private final String code;
    @Getter
    private final String details;
}
