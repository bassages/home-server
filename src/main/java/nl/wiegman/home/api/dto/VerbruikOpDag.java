package nl.wiegman.home.api.dto;

import nl.wiegman.home.model.Verbruik;

public class VerbruikOpDag extends Verbruik {

    private long datumtijd;

    public long getDatumtijd() {
        return datumtijd;
    }

    public void setDatumtijd(long datumtijd) {
        this.datumtijd = datumtijd;
    }
}
