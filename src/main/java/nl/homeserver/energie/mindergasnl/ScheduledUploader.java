package nl.homeserver.energie.mindergasnl;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

@AllArgsConstructor
class ScheduledUploader {

    private static final String THREE_AM = "0 0 3 * * *";

    private final MindergasnlService mindergasnlService;

    @Scheduled(cron = THREE_AM)
    public void uploadMeterstandWhenEnabled() {
        mindergasnlService.findOne()
                          .filter(MindergasnlSettings::isAutomatischUploaden)
                          .ifPresent(mindergasnlService::uploadMeterstand);
    }
}
