package nl.homeserver.energie.mindergasnl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledUploaderTest {

    @Mock
    MindergasnlService mindergasnlService;

    @InjectMocks
    ScheduledUploader scheduledUploader;

    @Test
    void givenNoMindergasnlSettingsExistWhenUploadMeterstandWhenEnabledThenNotUploaded() {
        // given
        when(mindergasnlService.findOne()).thenReturn(Optional.empty());

        // when
        scheduledUploader.uploadMeterstandWhenEnabled();

        // Then
        verify(mindergasnlService, times(0)).uploadMeterstand(any());
    }

    @Test
    void givenAutomaticUploadIsDisabledWhenUploadMeterstandWhenEnabledThenNotUploaded() {
        // given
        final MindergasnlSettings mindergasnlSettings = new MindergasnlSettings();
        mindergasnlSettings.setAutomatischUploaden(false);
        when(mindergasnlService.findOne()).thenReturn(Optional.of(mindergasnlSettings));

        // when
        scheduledUploader.uploadMeterstandWhenEnabled();

        // then
        verify(mindergasnlService, times(0)).uploadMeterstand(any());
    }

    @Test
    void givenAutomaticUploadIsEnabledWhenUploadMeterstandWhenEnabledThenUploaded() throws Exception {
        // given
        final MindergasnlSettings mindergasnlSettings = new MindergasnlSettings();
        mindergasnlSettings.setAutomatischUploaden(true);
        when(mindergasnlService.findOne()).thenReturn(Optional.of(mindergasnlSettings));

        // when
        scheduledUploader.uploadMeterstandWhenEnabled();

        // then
        verify(mindergasnlService).uploadMeterstand(mindergasnlSettings);
    }
}
