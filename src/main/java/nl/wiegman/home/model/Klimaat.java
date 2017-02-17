package nl.wiegman.home.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Klimaat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date datumtijd;

    @Column(precision = 4, scale = 2)
    private BigDecimal temperatuur;

    @Column(precision = 4, scale = 1)
    private BigDecimal luchtvochtigheid;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private KlimaatSensor klimaatSensor;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BigDecimal getTemperatuur() {
        return temperatuur;
    }

    public void setTemperatuur(BigDecimal temperatuur) {
        this.temperatuur = temperatuur;
    }

    public BigDecimal getLuchtvochtigheid() {
        return luchtvochtigheid;
    }

    public void setLuchtvochtigheid(BigDecimal luchtvochtigheid) {
        this.luchtvochtigheid = luchtvochtigheid;
    }

    public Date getDatumtijd() {
        return datumtijd;
    }

    public void setDatumtijd(Date datumtijd) {
        this.datumtijd = datumtijd;
    }

    public KlimaatSensor getKlimaatSensor() {
        return klimaatSensor;
    }

    public void setKlimaatSensor(KlimaatSensor klimaatSensor) {
        this.klimaatSensor = klimaatSensor;
    }
}
