package nl.wiegman.home.api;

import nl.wiegman.home.model.MindergasnlSettings;
import nl.wiegman.home.service.MindergasnlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mindergasnl")
public class MindergasnlSettingsServiceRest {

    @Autowired
    MindergasnlService mindergasnlService;

    @GetMapping
    public List<MindergasnlSettings> get() {
        return mindergasnlService.getAllSettings();
    }

    @PostMapping
    public MindergasnlSettings save(@RequestBody MindergasnlSettings mindergasnlSettings) {
        return mindergasnlService.save(mindergasnlSettings);
    }

}
