package nl.homeserver.klimaat;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
class KlimaatSensorDto {

    private String code;

    @Nullable
    private String omschrijving;
}
