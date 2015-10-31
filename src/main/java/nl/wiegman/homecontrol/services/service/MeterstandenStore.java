package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Meterstand;
import nl.wiegman.homecontrol.services.model.api.StroomMeterstand;
import nl.wiegman.homecontrol.services.model.api.OpgenomenVermogen;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class MeterstandenStore {

    public static final int MAX_NR_OF_ITEMS = 100000;

    public LinkedList<Meterstand> historie = new LinkedList<>();

    public void add(Meterstand meterstand) {
        if (historie.size() >= MAX_NR_OF_ITEMS) {
            historie.remove();
        }
        historie.add(meterstand);
    }

    public List<Meterstand> getAll() {
        return historie;
    }
}
