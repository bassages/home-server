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
class Dsmr42Reading {

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
