package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Kosten;
import nl.wiegman.homecontrol.services.model.api.MindergasnlSettings;
import nl.wiegman.homecontrol.services.repository.KostenRepository;
import nl.wiegman.homecontrol.services.repository.MindergasnlSettingsRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
public class MindergasnlSettingsService {

    @Inject
    MindergasnlSettingsRepository mindergasnlSettingsRepository;

    public List<MindergasnlSettings> getAll() {
        return mindergasnlSettingsRepository.findAll();
    }

    public MindergasnlSettings save(MindergasnlSettings mindergasnlSettings) {
        return mindergasnlSettingsRepository.save(mindergasnlSettings);
    }
}
