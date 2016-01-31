package nl.wiegman.homecontrol.services.service;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
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