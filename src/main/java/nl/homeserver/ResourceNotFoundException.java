package nl.homeserver;

import static java.lang.String.format;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(final String resourceName, final Object resourceIdentifier) {
        super(format("%s [%s] does not exist", resourceName, resourceIdentifier));
    }
}
