package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Kosten;
import nl.wiegman.homecontrol.services.repository.KostenRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Path("kosten")
@Produces(MediaType.APPLICATION_JSON)
public class KostenRest {

    @Inject
    KostenRepository kostenRepository;

    @Inject
    CacheService cacheService;

    @GET
    public List<Kosten> getAll() {
        return kostenRepository.findAll();
    }

    @POST
    public Kosten save(Kosten kosten) {
        Kosten result = kostenRepository.save(kosten);
        recalculateTotEnMet();
        cacheService.clearAll();
        return result;
    }

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") long id) {
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
                if (previousKosten.getTotEnMet() != totEnMet) {
                    previousKosten.setTotEnMet(totEnMet);
                    kostenRepository.save(previousKosten);
                }
            }

            if (i == (kostenList.size()-1)) {
                if (currentKosten.getTotEnMet() != Long.MAX_VALUE) {
                    currentKosten.setTotEnMet(Long.MAX_VALUE);
                    kostenRepository.save(currentKosten);
                }
            }
            previousKosten = currentKosten;
        }
    }
}
