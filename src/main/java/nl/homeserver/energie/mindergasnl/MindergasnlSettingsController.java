package nl.homeserver.energie.mindergasnl;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import nl.homeserver.config.Paths;

@RestController
@RequestMapping(Paths.API + "/mindergasnl")
@AllArgsConstructor
class MindergasnlSettingsController {

    private final MindergasnlService mindergasnlService;

    @GetMapping
    public MindergasnlSettings get() {
        return mindergasnlService.findOne().orElse(null);
    }

    @PostMapping
    public MindergasnlSettings save(final @RequestBody MindergasnlSettingsDto mindergasnlSettingsDto) {
        return mindergasnlService.save(fromDto(mindergasnlSettingsDto));
    }

    private MindergasnlSettings fromDto(final MindergasnlSettingsDto mindergasnlSettingsDto) {
        final MindergasnlSettings mindergasnlSettings = new MindergasnlSettings();
        mindergasnlSettings.setAutomatischUploaden(mindergasnlSettingsDto.isAutomatischUploaden());
        mindergasnlSettings.setAuthenticatietoken(mindergasnlSettingsDto.getAuthenticatietoken());
        return mindergasnlSettings;
    }
}
