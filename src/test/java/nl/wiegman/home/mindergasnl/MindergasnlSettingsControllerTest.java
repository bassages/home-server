package nl.wiegman.home.mindergasnl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MindergasnlSettingsControllerTest {

    @InjectMocks
    private MindergasnlSettingsController mindergasnlSettingsController;

    @Mock
    private MindergasnlService mindergasnlService;

    @Test
    public void whenSaveThenDelegatedToService() {
        MindergasnlSettings notSavedMindergasnlSettings = mock(MindergasnlSettings.class);
        MindergasnlSettings savedMindergasnlSettings = mock(MindergasnlSettings.class);

        when(mindergasnlService.save(notSavedMindergasnlSettings)).thenReturn(savedMindergasnlSettings);

        assertThat(mindergasnlSettingsController.save(notSavedMindergasnlSettings)).isSameAs(savedMindergasnlSettings);
    }

    @Test
    public void givenMindergasnlSettingsExistWhenGetThenRetrievedFromService() {
        MindergasnlSettings mindergasnlSettings = mock(MindergasnlSettings.class);

        when(mindergasnlService.findOne()).thenReturn(Optional.of(mindergasnlSettings));

        assertThat(mindergasnlSettingsController.get()).isSameAs(mindergasnlSettings);
    }

    @Test
    public void givenNoMindergasnlSettingsExistwhenGetThenNullReturned() {
        when(mindergasnlService.findOne()).thenReturn(Optional.empty());

        assertThat(mindergasnlSettingsController.get()).isNull();
    }
}