package nl.homeserver.energie.slimmemeter;

import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Setter
class LangeStroomStoring {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime datumtijdEinde;

    private Long duurVanStoringInSeconden;
}
