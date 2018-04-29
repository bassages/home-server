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

import lombok.Getter;
import lombok.Setter;

@Entity
public class Klimaat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private LocalDate datum;

    @Column(nullable = false, unique = true)
    @Getter
    private LocalDateTime datumtijd;

    @Getter
    @Setter
    @Column(precision = 4, scale = 2)
    private BigDecimal temperatuur;

    @Getter
    @Setter
    @Column(precision = 4, scale = 1)
    private BigDecimal luchtvochtigheid;

    @Fetch(FetchMode.JOIN)
    @ManyToOne(optional = false)
    @Getter
    @Setter
    @JsonIgnore
    private KlimaatSensor klimaatSensor;

    public void setDatumtijd(LocalDateTime datumtijd) {
        this.datumtijd = datumtijd;
        if (datumtijd != null) {
            this.datum = datumtijd.toLocalDate();
        } else {
            this.datum = null;
        }
    }
}
