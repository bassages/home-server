package nl.homeserver.cache;

import lombok.RequiredArgsConstructor;
import nl.homeserver.config.Paths;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Paths.API + "/cache")
@RequiredArgsConstructor
class CacheController {

    private final CacheService cacheService;

    @PostMapping(path = "clearAll")
    public void clearAll() {
        cacheService.clearAll();
    }
}
