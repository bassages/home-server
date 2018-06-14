package nl.homeserver.cache;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheService cacheService;

    public CacheController(final CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @PostMapping(path = "clearAll")
    public void clearAll() {
        cacheService.clearAll();
    }
}