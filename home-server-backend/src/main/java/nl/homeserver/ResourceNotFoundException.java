package nl.homeserver;

import static java.lang.String.format;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Object resourceIdentifier) {
        super(format("%s [%s] does not exist", resourceName, resourceIdentifier));
    }
}
