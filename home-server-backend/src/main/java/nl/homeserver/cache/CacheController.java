package nl.homeserver.cache;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.homeserver.config.Paths;

@RestController
@RequestMapping(Paths.API + "/cache")
class CacheController {

    private final CacheService cacheService;

    CacheController(final CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @PostMapping(path = "clearAll")
    void clearAll() {
        cacheService.clearAll();
    }
}