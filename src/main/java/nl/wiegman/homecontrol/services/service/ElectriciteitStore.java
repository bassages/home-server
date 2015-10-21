package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.apimodel.OpgenomenVermogen;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Component
public class ElectriciteitStore {

    public static final int MAX_NR_OF_ITEMS = 1000;

    public LinkedList<OpgenomenVermogen> opgenomenVermogenHistorie = new LinkedList<>();

    public void add(OpgenomenVermogen opgenomenVermogen) {
        if (opgenomenVermogenHistorie.size() >= MAX_NR_OF_ITEMS) {
            opgenomenVermogenHistorie.remove();
        }
        opgenomenVermogenHistorie.add(opgenomenVermogen);
    }

    public List<OpgenomenVermogen> getAll() {
        return opgenomenVermogenHistorie;
    }
}
