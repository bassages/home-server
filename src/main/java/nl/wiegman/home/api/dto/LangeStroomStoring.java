package nl.wiegman.home.api.dto;

import java.util.Date;

public class LangeStroomStoring {
    private Date datumtijdEinde;
    private Long duurVanStoringInSeconden;

    public Date getDatumtijdEinde() {
        return datumtijdEinde;
    }

    public void setDatumtijdEinde(Date datumtijdEinde) {
        this.datumtijdEinde = datumtijdEinde;
    }

    public Long getDuurVanStoringInSeconden() {
        return duurVanStoringInSeconden;
    }

    public void setDuurVanStoringInSeconden(Long duurVanStoringInSeconden) {
        this.duurVanStoringInSeconden = duurVanStoringInSeconden;
    }
}
