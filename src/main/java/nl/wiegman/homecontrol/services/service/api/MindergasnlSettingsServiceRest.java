package nl.wiegman.homecontrol.services.service.api;

import nl.wiegman.homecontrol.services.model.api.Kosten;
import nl.wiegman.homecontrol.services.model.api.MindergasnlSettings;
import nl.wiegman.homecontrol.services.service.KostenService;
import nl.wiegman.homecontrol.services.service.MindergasnlSettingsService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Path("mindergasnl")
@Produces(MediaType.APPLICATION_JSON)
public class MindergasnlSettingsServiceRest {

    @Inject
    MindergasnlSettingsService mindergasnlSettingsService;

    @GET
    public List<MindergasnlSettings> get() {
        return mindergasnlSettingsService.getAll();
    }

    @POST
    public MindergasnlSettings save(MindergasnlSettings mindergasnlSettings) {
        return mindergasnlSettingsService.save(mindergasnlSettings);
    }

}
