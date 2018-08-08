package nl.homeserver.mindergasnl;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mindergasnl")
public class MindergasnlSettingsController {

    private final MindergasnlService mindergasnlService;

    public MindergasnlSettingsController(final MindergasnlService mindergasnlService) {
        this.mindergasnlService = mindergasnlService;
    }

    @GetMapping
    public MindergasnlSettings get() {
        return mindergasnlService.findOne().orElse(null);
    }

    @PostMapping
    public MindergasnlSettings save(final @RequestBody MindergasnlSettings mindergasnlSettings) {
        return mindergasnlService.save(mindergasnlSettings);
    }
}
