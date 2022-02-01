package nl.homeserver.energie.mindergasnl;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class MindergasnlSettingsDto {
    private boolean automatischUploaden;
    private String authenticatietoken;
}
