package nl.homeserver.cache;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CacheControllerTest {

    @InjectMocks
    private CacheController cacheController;

    @Mock
    private CacheService cacheService;

    @Test
    public void whenClearAllThenDelegatedToCacheService() {
        cacheController.clearAll();
        verify(cacheService).clearAll();
    }
}