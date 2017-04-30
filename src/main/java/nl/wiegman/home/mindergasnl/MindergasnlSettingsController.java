package nl.wiegman.home.mindergasnl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mindergasnl")
public class MindergasnlSettingsController {

    private final MindergasnlService mindergasnlService;

    @Autowired
    public MindergasnlSettingsController(MindergasnlService mindergasnlService) {
        this.mindergasnlService = mindergasnlService;
    }

    @GetMapping
    public List<MindergasnlSettings> get() {
        return mindergasnlService.getAllSettings();
    }

    @PostMapping
    public MindergasnlSettings save(@RequestBody MindergasnlSettings mindergasnlSettings) {
        return mindergasnlService.save(mindergasnlSettings);
    }

}
