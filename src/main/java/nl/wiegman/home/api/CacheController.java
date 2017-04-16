package nl.wiegman.home.api;

import nl.wiegman.home.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    @Autowired
    CacheService cacheService;

    @PostMapping(path = "clearAll")
    public void clearAll() {
        cacheService.clearAll();
    }
}