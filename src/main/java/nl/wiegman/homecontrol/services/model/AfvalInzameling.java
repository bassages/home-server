package nl.wiegman.homecontrol.services.model;

import java.util.Date;

/**
 *
 */
public class AfvalInzameling {

    private Date datum;
    private String omschrijving;

    public Date getDatum() {
        return datum;
    }

    public void setDatum(Date datum) {
        this.datum = datum;
    }

    public String getOmschrijving() {
        return omschrijving;
    }

    public void setOmschrijving(String omschrijving) {
        this.omschrijving = omschrijving;
    }
}
