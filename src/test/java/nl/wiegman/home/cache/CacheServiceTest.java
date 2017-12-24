package nl.wiegman.home.cache;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@RunWith(MockitoJUnitRunner.class)
public class CacheServiceTest {

    @InjectMocks
    private CacheService cacheService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache1, cache2, cache3;

    @Test
    public void givenSingleCacheWhenClearThenCleared() {
        String nameOfCache1 = "nameOfCache1";
        when(cacheManager.getCache(nameOfCache1)).thenReturn(cache1);

        cacheService.clear(nameOfCache1);

        verify(cache1).clear();
    }

    @Test
    public void givenThreeCachesWhenClearAllThenEveryCacheCleared() {
        String nameOfCache1 = "nameOfCache1";
        String nameOfCache2 = "nameOfCache2";
        String nameOfCache3 = "nameOfCache3";

        when(cacheManager.getCacheNames()).thenReturn(asList(nameOfCache1, nameOfCache2, nameOfCache3));

        when(cacheManager.getCache(nameOfCache1)).thenReturn(cache1);
        when(cacheManager.getCache(nameOfCache2)).thenReturn(cache2);
        when(cacheManager.getCache(nameOfCache3)).thenReturn(cache3);

        cacheService.clearAll();

        verify(cache1).clear();
        verify(cache2).clear();
        verify(cache3).clear();
    }
}