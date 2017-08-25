package nl.wiegman.home.mindergasnl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
