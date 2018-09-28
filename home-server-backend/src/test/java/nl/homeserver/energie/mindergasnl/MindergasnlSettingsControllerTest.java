package nl.homeserver.energie.mindergasnl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MindergasnlSettingsControllerTest {

    @InjectMocks
    private MindergasnlSettingsController mindergasnlSettingsController;

    @Mock
    private MindergasnlService mindergasnlService;

    @Test
    public void whenSaveThenDelegatedToService() {
        final MindergasnlSettings savedMindergasnlSettings = mock(MindergasnlSettings.class);

        when(mindergasnlService.save(any())).thenReturn(savedMindergasnlSettings);

        assertThat(mindergasnlSettingsController.save(mock(MindergasnlSettingsDto.class))).isSameAs(savedMindergasnlSettings);
    }

    @Test
    public void givenMindergasnlSettingsExistWhenGetThenRetrievedFromService() {
        final MindergasnlSettings mindergasnlSettings = mock(MindergasnlSettings.class);

        when(mindergasnlService.findOne()).thenReturn(Optional.of(mindergasnlSettings));

        assertThat(mindergasnlSettingsController.get()).isSameAs(mindergasnlSettings);
    }

    @Test
    public void givenNoMindergasnlSettingsExistWhenGetThenNullReturned() {
        when(mindergasnlService.findOne()).thenReturn(Optional.empty());

        assertThat(mindergasnlSettingsController.get()).isNull();
    }
}