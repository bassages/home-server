package nl.wiegman.home.service;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class CacheService {

    @Inject
    CacheManager cacheManager;

    public void clearAll() {
        cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

    public void clear(String cacheName) {
        cacheManager.getCache(cacheName).clear();
    }
}