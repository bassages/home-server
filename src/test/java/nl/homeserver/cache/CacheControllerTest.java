package nl.homeserver.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CacheControllerTest {

    @InjectMocks
    CacheController cacheController;

    @Mock
    CacheService cacheService;

    @Test
    void whenClearAllThenDelegatedToCacheService() {
        cacheController.clearAll();
        verify(cacheService).clearAll();
    }
}
