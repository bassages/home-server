package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.apimodel.OpgenomenVermogen;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ElectriciteitStore {

    public List<OpgenomenVermogen> opgenomenVermogenHistorie = new ArrayList<>();

    public void add(OpgenomenVermogen opgenomenVermogen) {
        opgenomenVermogenHistorie.add(opgenomenVermogen);
    }

    public List<OpgenomenVermogen> getAll() {
        return opgenomenVermogenHistorie;
    }
}
