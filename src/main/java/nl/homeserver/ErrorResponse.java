package nl.homeserver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class ErrorResponse {

    private final String code;
    private final String details;
}
