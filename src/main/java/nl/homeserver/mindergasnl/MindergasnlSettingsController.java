package nl.homeserver.mindergasnl;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mindergasnl")
public class MindergasnlSettingsController {

    private final MindergasnlService mindergasnlService;

    public MindergasnlSettingsController(MindergasnlService mindergasnlService) {
        this.mindergasnlService = mindergasnlService;
    }

    @GetMapping
    public MindergasnlSettings get() {
        return mindergasnlService.findOne().orElse(null);
    }

    @PostMapping
    public MindergasnlSettings save(@RequestBody MindergasnlSettings mindergasnlSettings) {
        return mindergasnlService.save(mindergasnlSettings);
    }

}
