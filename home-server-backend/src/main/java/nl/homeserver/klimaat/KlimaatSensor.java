package nl.homeserver.klimaat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
public class KlimaatSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private short id;

    @Column(unique = true, nullable = false)
    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private String omschrijving;
}
