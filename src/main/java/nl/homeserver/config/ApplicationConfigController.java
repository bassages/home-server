package nl.homeserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Paths.API + "/config")
class ApplicationConfigController {

    private final boolean dashboardClimateEnabled;
    private final boolean dashboardGasEnabled;

    ApplicationConfigController(@Value("${home-server.dashboard.climate.enabled:true}") final boolean dashboardClimateEnabled,
                                @Value("${home-server.dashboard.gas.enabled:true}") final boolean dashboardGasEnabled) {
        this.dashboardClimateEnabled = dashboardClimateEnabled;
        this.dashboardGasEnabled = dashboardGasEnabled;
    }

    @GetMapping
    public ApplicationConfigDto getConfig() {
        return new ApplicationConfigDto(dashboardClimateEnabled, dashboardGasEnabled);
    }

    record ApplicationConfigDto(boolean dashboardClimateEnabled, boolean dashboardGasEnabled) {
    }
}
