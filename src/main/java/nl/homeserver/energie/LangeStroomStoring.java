package nl.homeserver.energie;

import java.util.Date;

import lombok.Setter;

public class LangeStroomStoring {

    @Setter
    private Date datumtijdEinde;

    @Setter
    private Long duurVanStoringInSeconden;
}
