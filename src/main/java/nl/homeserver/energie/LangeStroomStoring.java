package nl.homeserver.energie;

import java.util.Date;

import lombok.Data;

@Data
public class LangeStroomStoring {

    private Date datumtijdEinde;
    private Long duurVanStoringInSeconden;
}
