package nl.homeserver.cache;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @InjectMocks
    CacheService cacheService;

    @Mock
    CacheManager cacheManager;

    @Mock
    Cache cache1, cache2, cache3;

    @Test
    void givenSingleCacheWhenClearThenCleared() {
        final String nameOfCache1 = "nameOfCache1";
        when(cacheManager.getCache(nameOfCache1)).thenReturn(cache1);

        cacheService.clear(nameOfCache1);

        verify(cache1).clear();
    }

    @Test
    void givenThreeCachesWhenClearAllThenEveryCacheCleared() {
        final String nameOfCache1 = "nameOfCache1";
        final String nameOfCache2 = "nameOfCache2";
        final String nameOfCache3 = "nameOfCache3";

        when(cacheManager.getCacheNames()).thenReturn(List.of(nameOfCache1, nameOfCache2, nameOfCache3));

        when(cacheManager.getCache(nameOfCache1)).thenReturn(cache1);
        when(cacheManager.getCache(nameOfCache2)).thenReturn(cache2);
        when(cacheManager.getCache(nameOfCache3)).thenReturn(cache3);

        cacheService.clearAll();

        verify(cache1).clear();
        verify(cache2).clear();
        verify(cache3).clear();
    }
}
