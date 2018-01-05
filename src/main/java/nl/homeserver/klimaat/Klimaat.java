package nl.homeserver.klimaat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Klimaat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private LocalDate datum;

    @Column(nullable = false, unique = true)
    private LocalDateTime datumtijd;

    @Column(precision = 4, scale = 2)
    private BigDecimal temperatuur;

    @Column(precision = 4, scale = 1)
    private BigDecimal luchtvochtigheid;

    @JsonIgnore
    @Fetch(FetchMode.JOIN)
    @ManyToOne(optional = false)
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

    public LocalDateTime getDatumtijd() {
        return datumtijd;
    }

    public void setDatumtijd(LocalDateTime datumtijd) {
        this.datumtijd = datumtijd;
        this.datum = datumtijd.toLocalDate();
    }

    public KlimaatSensor getKlimaatSensor() {
        return klimaatSensor;
    }

    public void setKlimaatSensor(KlimaatSensor klimaatSensor) {
        this.klimaatSensor = klimaatSensor;
    }
}
