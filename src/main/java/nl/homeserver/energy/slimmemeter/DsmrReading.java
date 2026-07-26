package nl.homeserver.energy.slimmemeter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@ToString
class DsmrReading {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Getter
    private LocalDateTime datumtijd;

    @Getter
    private int stroomOpgenomenVermogenInWatt;

    @Getter
    private BigDecimal stroomTarief1;

    @Getter
    private BigDecimal stroomTarief2;

    @Getter
    private Integer stroomTariefIndicator;

    @Getter
    private BigDecimal gas;

    @Getter
    private String meterIdentificatieStroom;

    @Getter
    private String meterIdentificatieGas;

    private Integer aantalStroomStoringenInAlleFases;

    private Integer aantalSpanningsDippenInFaseL1;
    private Integer aantalSpanningsDippenInFaseL2;
    private Integer aantalSpanningsDippenInFaseL3;

    private String tekstBericht;

    private String tekstBerichtCodes;

    private Integer aantalLangeStroomStoringenInAlleFases;

    private List<LangeStroomStoring> langeStroomStoringen;

    private Integer voltageL1;
    private Integer voltageL2;
    private Integer voltageL3;

    private Integer directGeleverdVermogenL1InWatt;
    private Integer directGeleverdVermogenL2InWatt;
    private Integer directGeleverdVermogenL3InWatt;

    private Integer directTeruggeleverdVermogenL1InWatt;
    private Integer directTeruggeleverdVermogenL2InWatt;
    private Integer directTeruggeleverdVermogenL3InWatt;

}
