package nl.homeserver.energie;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class Dsmr42Reading {

    @Getter
    @Setter
    private long datumtijd;

    @Getter
    @Setter
    private int stroomOpgenomenVermogenInWatt;

    @Getter
    @Setter
    private BigDecimal stroomTarief1;

    @Getter
    @Setter
    private BigDecimal stroomTarief2;

    @Getter
    @Setter
    private Integer stroomTariefIndicator;

    @Getter
    @Setter
    private BigDecimal gas;

    @Setter
    private String meterIdentificatieStroom;

    @Setter
    private String meterIdentificatieGas;

    @Setter
    private Integer aantalStroomStoringenInAlleFases;

    @Setter
    private Integer aantalSpanningsDippenInFaseL1;

    @Setter
    private Integer aantalSpanningsDippenInFaseL2;

    @Setter
    private String tekstBericht;

    @Setter
    private String tekstBerichtCodes;

    @Setter
    private Integer aantalLangeStroomStoringenInAlleFases;

    @Setter
    private List<LangeStroomStoring> langeStroomStoringen;
}
