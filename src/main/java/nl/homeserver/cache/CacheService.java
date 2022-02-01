package nl.homeserver.cache;

import static java.lang.String.format;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;

    public void clearAll() {
        cacheManager.getCacheNames().forEach(this::clear);
    }

    public void clear(final String nameOfCacheToClear) {
        final Cache cache = cacheManager.getCache(nameOfCacheToClear);
        Assert.notNull(cache, format("cache with name %s does not exist", nameOfCacheToClear));
        cache.clear();
    }
}
