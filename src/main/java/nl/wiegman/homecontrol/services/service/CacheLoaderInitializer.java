package nl.wiegman.homecontrol.services.service;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Component
public class CacheLoaderInitializer {

    @Inject
    private CacheLoader cacheLoader;

    @PostConstruct
    public void initialize() {
//        cacheLoader.loadCacheAsync();
    }
}
