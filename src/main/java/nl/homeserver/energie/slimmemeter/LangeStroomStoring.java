package nl.homeserver.energie.slimmemeter;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Setter;

class LangeStroomStoring {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Setter
    private LocalDateTime datumtijdEinde;

    @Setter
    private Long duurVanStoringInSeconden;
}
