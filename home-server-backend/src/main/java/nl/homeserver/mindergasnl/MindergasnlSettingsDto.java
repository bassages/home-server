package nl.homeserver.mindergasnl;

import lombok.Getter;
import lombok.Setter;

class MindergasnlSettingsDto {

    @Getter
    @Setter
    private boolean automatischUploaden;

    @Getter
    @Setter
    private String authenticatietoken;
}
