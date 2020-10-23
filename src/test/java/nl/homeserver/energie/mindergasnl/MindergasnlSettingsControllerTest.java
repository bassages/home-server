package nl.homeserver.energie.mindergasnl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MindergasnlSettingsControllerTest {

    @InjectMocks
    MindergasnlSettingsController mindergasnlSettingsController;

    @Mock
    MindergasnlService mindergasnlService;

    @Test
    void whenSaveThenDelegatedToService() {
        final MindergasnlSettings savedMindergasnlSettings = mock(MindergasnlSettings.class);

        when(mindergasnlService.save(any())).thenReturn(savedMindergasnlSettings);

        assertThat(mindergasnlSettingsController.save(mock(MindergasnlSettingsDto.class))).isSameAs(savedMindergasnlSettings);
    }

    @Test
    void givenMindergasnlSettingsExistWhenGetThenRetrievedFromService() {
        final MindergasnlSettings mindergasnlSettings = mock(MindergasnlSettings.class);

        when(mindergasnlService.findOne()).thenReturn(Optional.of(mindergasnlSettings));

        assertThat(mindergasnlSettingsController.get()).isSameAs(mindergasnlSettings);
    }

    @Test
    void givenNoMindergasnlSettingsExistWhenGetThenNullReturned() {
        when(mindergasnlService.findOne()).thenReturn(Optional.empty());

        assertThat(mindergasnlSettingsController.get()).isNull();
    }
}
