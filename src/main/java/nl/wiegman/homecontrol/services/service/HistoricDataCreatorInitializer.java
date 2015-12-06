package nl.wiegman.homecontrol.services.service;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Component
public class HistoricDataCreatorInitializer {

    @Inject
    private DummyDataService dummyDataService;

    @PostConstruct
    public void initialize() {
        dummyDataService.generateHistoricData();
    }
}
