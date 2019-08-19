package nl.homeserver.cache;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import nl.homeserver.config.Paths;

@RestController
@RequestMapping(Paths.API + "/cache")
@AllArgsConstructor
class CacheController {

    private final CacheService cacheService;

    @PostMapping(path = "clearAll")
    public void clearAll() {
        cacheService.clearAll();
    }
}