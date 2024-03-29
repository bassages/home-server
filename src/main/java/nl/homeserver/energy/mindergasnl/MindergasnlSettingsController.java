package nl.homeserver.energy.mindergasnl;

import lombok.RequiredArgsConstructor;
import nl.homeserver.config.Paths;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Paths.API + "/mindergasnl")
@RequiredArgsConstructor
class MindergasnlSettingsController {

    private final MindergasnlService mindergasnlService;

    @GetMapping
    public MindergasnlSettings get() {
        return mindergasnlService.findSettings().orElse(null);
    }

    @PostMapping
    public MindergasnlSettings save(final @RequestBody MindergasnlSettingsDto mindergasnlSettingsDto) {
        return mindergasnlService.save(fromDto(mindergasnlSettingsDto));
    }

    private MindergasnlSettings fromDto(final MindergasnlSettingsDto mindergasnlSettingsDto) {
        final MindergasnlSettings mindergasnlSettings = new MindergasnlSettings();
        mindergasnlSettings.setAutomatischUploaden(mindergasnlSettingsDto.automatischUploaden());
        mindergasnlSettings.setAuthenticatietoken(mindergasnlSettingsDto.authenticatietoken());
        return mindergasnlSettings;
    }
}
