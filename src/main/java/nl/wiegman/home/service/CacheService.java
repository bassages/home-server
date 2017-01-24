package nl.wiegman.home.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    @Autowired
    CacheManager cacheManager;

    public void clearAll() {
        cacheManager.getCacheNames().forEach(this::clear);
    }

    public void clear(String cacheName) {
        cacheManager.getCache(cacheName).clear();
    }
}