package nl.homeserver.energie.mindergasnl;

record MindergasnlSettingsDto(
        boolean automatischUploaden,
        String authenticatietoken
) { }
