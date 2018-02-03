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

    @Getter
    @Setter
    private String tariefIndicatorStroom;

    private String meterIdentificatieStroom;
    private String meterIdentificatieGas;
    private Integer aantalStroomStoringenInAlleFases;
    private Integer aantalSpanningsDippenInFaseL1;
    private Integer aantalSpanningsDippenInFaseL2;
    private String tekstBericht;
    private String tekstBerichtCodes;
    private Integer aantalLangeStroomStoringenInAlleFases;
    private List<LangeStroomStoring> langeStroomStoringen;
}
