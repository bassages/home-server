package nl.wiegman.home.service;

import nl.wiegman.home.model.api.Kosten;
import nl.wiegman.home.repository.KostenRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
public class KostenService {

    public static final long SINT_JUTTEMIS = 7258114800000l;

    @Inject
    KostenRepository kostenRepository;

    @Inject
    CacheService cacheService;

    public List<Kosten> getAll() {
        return kostenRepository.findAll();
    }

    public Kosten save(Kosten kosten) {
        if (kosten.getTotEnMet() == null) {
            kosten.setTotEnMet(0l);
        }
        Kosten result = kostenRepository.save(kosten);
        recalculateTotEnMet();
        cacheService.clearAll();
        return result;
    }

    public void delete(long id) {
        kostenRepository.delete(id);
        recalculateTotEnMet();
        cacheService.clearAll();
    }

    protected void recalculateTotEnMet() {
        List<Kosten> kostenList = kostenRepository.findAll(new Sort(Sort.Direction.ASC, "van"));

        Kosten previousKosten = null;
        for (int i=0; i<kostenList.size(); i++) {
            Kosten currentKosten = kostenList.get(i);
            if (previousKosten != null) {
                long totEnMet = currentKosten.getVan() - 1;
                if (ObjectUtils.notEqual(previousKosten.getTotEnMet(), totEnMet)) {
                    previousKosten.setTotEnMet(totEnMet);
                    kostenRepository.save(previousKosten);
                }
            }

            if (i == (kostenList.size()-1)) {
                if (ObjectUtils.notEqual(currentKosten.getTotEnMet(), SINT_JUTTEMIS)) {
                    currentKosten.setTotEnMet(SINT_JUTTEMIS);
                    kostenRepository.save(currentKosten);
                }
            }
            previousKosten = currentKosten;
        }
    }
}
