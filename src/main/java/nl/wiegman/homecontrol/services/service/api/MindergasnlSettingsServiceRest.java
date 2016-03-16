package nl.wiegman.homecontrol.services.service.api;

import nl.wiegman.homecontrol.services.model.api.MindergasnlSettings;
import nl.wiegman.homecontrol.services.service.MindergasnlService;
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
    MindergasnlService mindergasnlService;

    @GET
    public List<MindergasnlSettings> get() {
        return mindergasnlService.getAllSettings();
    }

    @POST
    public MindergasnlSettings save(MindergasnlSettings mindergasnlSettings) {
        return mindergasnlService.save(mindergasnlSettings);
    }

}
