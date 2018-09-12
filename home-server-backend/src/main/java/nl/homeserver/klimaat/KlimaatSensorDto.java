package nl.homeserver.klimaat;

import javax.annotation.Nullable;

import lombok.Getter;
import lombok.Setter;

public class KlimaatSensorDto {

    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    @Nullable
    private String omschrijving;
}
