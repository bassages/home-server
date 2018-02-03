package nl.homeserver.energie;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class Dsmr42Reading {

    private long datumtijd;
    private int stroomOpgenomenVermogenInWatt;
    private BigDecimal stroomTarief1;
    private BigDecimal stroomTarief2;
    private Integer stroomTariefIndicator;
    private BigDecimal gas;
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
