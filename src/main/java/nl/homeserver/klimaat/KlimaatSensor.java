package nl.homeserver.klimaat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class KlimaatSensor {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private short id;

    @Column(unique = true, nullable = false)
    private String code;

    private String omschrijving;

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public String getOmschrijving() {
        return omschrijving;
    }

    public void setOmschrijving(String omschrijving) {
        this.omschrijving = omschrijving;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
