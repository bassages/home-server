package nl.homeserver.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final CacheManager cacheManager;

    @Autowired
    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void clearAll() {
        cacheManager.getCacheNames().forEach(this::clear);
    }

    public void clear(String nameOfCacheToClear) {
        cacheManager.getCache(nameOfCacheToClear).clear();
    }
}