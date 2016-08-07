package nl.wiegman.home.service.api;

import nl.wiegman.home.model.MindergasnlSettings;
import nl.wiegman.home.service.MindergasnlService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
