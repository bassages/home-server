package nl.homeserver.climate;

import javax.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Nullable
    private String omschrijving;

    static KlimaatSensorBuilder aKlimaatSensor() {
        return KlimaatSensor.builder();
    }
}
