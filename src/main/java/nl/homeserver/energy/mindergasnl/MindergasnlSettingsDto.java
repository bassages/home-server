package nl.homeserver.energy.mindergasnl;

record MindergasnlSettingsDto(
        boolean automatischUploaden,
        String authenticatietoken
) { }
