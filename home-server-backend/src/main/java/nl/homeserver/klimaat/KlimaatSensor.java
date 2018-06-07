package nl.homeserver.klimaat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@ToString
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
