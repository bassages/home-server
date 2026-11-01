package nl.homeserver.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationConfigControllerTest {

    @Test
    void whenClimateIsEnabledThenConfigExposesIt() {
        final ApplicationConfigController controller = new ApplicationConfigController(true, true);

        assertThat(controller.getConfig().dashboardClimateEnabled()).isTrue();
        assertThat(controller.getConfig().dashboardGasEnabled()).isTrue();
    }

    @Test
    void whenClimateIsDisabledThenConfigExposesIt() {
        final ApplicationConfigController controller = new ApplicationConfigController(false, true);

        assertThat(controller.getConfig().dashboardClimateEnabled()).isFalse();
        assertThat(controller.getConfig().dashboardGasEnabled()).isTrue();
    }

    @Test
    void whenGasIsDisabledThenConfigExposesIt() {
        final ApplicationConfigController controller = new ApplicationConfigController(true, false);

        assertThat(controller.getConfig().dashboardGasEnabled()).isFalse();
    }
}
