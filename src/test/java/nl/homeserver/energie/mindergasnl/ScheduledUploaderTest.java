package nl.homeserver.energie.mindergasnl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ScheduledUploaderTest {

    @Mock
    private MindergasnlService mindergasnlService;

    @InjectMocks
    private ScheduledUploader scheduledUploader;

    @Test
    public void givenNoMindergasnlSettingsExistWhenUploadMeterstandWhenEnabledThenNotUploaded() {
        // given
        when(mindergasnlService.findOne()).thenReturn(Optional.empty());

        // when
        scheduledUploader.uploadMeterstandWhenEnabled();

        // Then
        verify(mindergasnlService, times(0)).uploadMeterstand(any());
    }

    @Test
    public void givenAutomaticUploadIsDisabledWhenUploadMeterstandWhenEnabledThenNotUploaded() {
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
    public void givenAutomaticUploadIsEnabledWhenUploadMeterstandWhenEnabledThenUploaded() throws Exception {
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
