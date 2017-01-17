package nl.wiegman.home.service;

import nl.wiegman.home.model.Energiecontract;
import nl.wiegman.home.repository.EnergiecontractRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnergiecontractService {

    public static final long SINT_JUTTEMIS = 7258114800000l;

    @Autowired
    EnergiecontractRepository energiecontractRepository;

    @Autowired
    CacheService cacheService;

    public List<Energiecontract> getAll() {
        return energiecontractRepository.findAll();
    }

    public Energiecontract save(Energiecontract energiecontract) {
        if (energiecontract.getTotEnMet() == null) {
            energiecontract.setTotEnMet(0l);
        }
        Energiecontract result = energiecontractRepository.save(energiecontract);
        recalculateTotEnMet();
        cacheService.clearAll();
        return result;
    }

    public void delete(long id) {
        energiecontractRepository.delete(id);
        recalculateTotEnMet();
        cacheService.clearAll();
    }

    protected void recalculateTotEnMet() {
        List<Energiecontract> energiecontractList = energiecontractRepository.findAll(new Sort(Sort.Direction.ASC, "van"));

        Energiecontract previousEnergiecontract = null;
        for (int i=0; i< energiecontractList.size(); i++) {
            Energiecontract currentEnergiecontract = energiecontractList.get(i);
            if (previousEnergiecontract != null) {
                long totEnMet = currentEnergiecontract.getVan() - 1;
                if (ObjectUtils.notEqual(previousEnergiecontract.getTotEnMet(), totEnMet)) {
                    previousEnergiecontract.setTotEnMet(totEnMet);
                    energiecontractRepository.save(previousEnergiecontract);
                }
            }

            if (i == (energiecontractList.size()-1)) {
                if (ObjectUtils.notEqual(currentEnergiecontract.getTotEnMet(), SINT_JUTTEMIS)) {
                    currentEnergiecontract.setTotEnMet(SINT_JUTTEMIS);
                    energiecontractRepository.save(currentEnergiecontract);
                }
            }
            previousEnergiecontract = currentEnergiecontract;
        }
    }
}
