package nl.homeserver.energie.mindergasnl;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// If a Service/Component annotation is not present, the scheduling does not work.
// Strangely enough the integrationtest is actually passing without this annotation?!?.
@Component
@AllArgsConstructor
class ScheduledUploader {
    private final MindergasnlService mindergasnlService;

    @Scheduled(cron = "${mindergasnl.scheduleduploader.cron}")
    void uploadMeterstandWhenEnabled() {
        mindergasnlService.findOne()
                          .filter(MindergasnlSettings::isAutomatischUploaden)
                          .ifPresent(mindergasnlService::uploadMeterstand);
    }
}
