package nl.homeserver.energy.slimmemeter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
class DsmrReading {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime datumtijd;

    private int stroomOpgenomenVermogenInWatt;

    private BigDecimal stroomTarief1;
    private BigDecimal stroomTarief2;
    private Integer stroomTariefIndicator;
    private BigDecimal gas;

    private String meterIdentificatieStroom;
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

    private Integer instantaneousCurrentL1Ampere;
    private Integer instantaneousCurrentL2Ampere;
    private Integer instantaneousCurrentL3Ampere;

    private Integer directGeleverdVermogenL1InWatt;
    private Integer directGeleverdVermogenL2InWatt;
    private Integer directGeleverdVermogenL3InWatt;

    private Integer directTeruggeleverdVermogenL1InWatt;
    private Integer directTeruggeleverdVermogenL2InWatt;
    private Integer directTeruggeleverdVermogenL3InWatt;
}
