package nl.homeserver.klimaat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@ToString
@Entity
class Klimaat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @SuppressWarnings("FieldCanBeLocal")
    @Column
    private LocalDate datum;

    @Column(nullable = false)
    @Getter
    private LocalDateTime datumtijd;

    @Getter
    @Setter
    @Nullable
    @Column(precision = 4, scale = 2)
    private BigDecimal temperatuur;

    @Getter
    @Setter
    @Nullable
    @Column(precision = 4, scale = 1)
    private BigDecimal luchtvochtigheid;

    @Fetch(FetchMode.JOIN)
    @ManyToOne(optional = false)
    @Getter
    @Setter
    @JsonIgnore
    private KlimaatSensor klimaatSensor;

    public void setDatumtijd(@Nullable final LocalDateTime datumtijd) {
        this.datumtijd = datumtijd;
        this.datum = datumtijd == null ? null : datumtijd.toLocalDate();
    }
}
