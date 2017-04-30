package nl.wiegman.home.energie;

import java.math.BigDecimal;
import java.util.List;

public class Dsmr42ReadingDto {

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

    public long getDatumtijd() {
        return datumtijd;
    }

    public void setDatumtijd(long datumtijd) {
        this.datumtijd = datumtijd;
    }

    public int getStroomOpgenomenVermogenInWatt() {
        return stroomOpgenomenVermogenInWatt;
    }

    public void setStroomOpgenomenVermogenInWatt(int stroomOpgenomenVermogenInWatt) {
        this.stroomOpgenomenVermogenInWatt = stroomOpgenomenVermogenInWatt;
    }

    public BigDecimal getStroomTarief1() {
        return stroomTarief1;
    }

    public void setStroomTarief1(BigDecimal stroomTarief1) {
        this.stroomTarief1 = stroomTarief1;
    }

    public BigDecimal getStroomTarief2() {
        return stroomTarief2;
    }

    public void setStroomTarief2(BigDecimal stroomTarief2) {
        this.stroomTarief2 = stroomTarief2;
    }

    public BigDecimal getGas() {
        return gas;
    }

    public void setGas(BigDecimal gas) {
        this.gas = gas;
    }

    public Integer getStroomTariefIndicator() {
        return stroomTariefIndicator;
    }

    public void setStroomTariefIndicator(Integer stroomTariefIndicator) {
        this.stroomTariefIndicator = stroomTariefIndicator;
    }

    public Integer getAantalStroomStoringenInAlleFases() {
        return aantalStroomStoringenInAlleFases;
    }

    public void setAantalStroomStoringenInAlleFases(Integer aantalStroomStoringenInAlleFases) {
        this.aantalStroomStoringenInAlleFases = aantalStroomStoringenInAlleFases;
    }

    public Integer getAantalSpanningsDippenInFaseL1() {
        return aantalSpanningsDippenInFaseL1;
    }

    public void setAantalSpanningsDippenInFaseL1(Integer aantalSpanningsDippenInFaseL1) {
        this.aantalSpanningsDippenInFaseL1 = aantalSpanningsDippenInFaseL1;
    }

    public Integer getAantalSpanningsDippenInFaseL2() {
        return aantalSpanningsDippenInFaseL2;
    }

    public void setAantalSpanningsDippenInFaseL2(Integer aantalSpanningsDippenInFaseL2) {
        this.aantalSpanningsDippenInFaseL2 = aantalSpanningsDippenInFaseL2;
    }

    public String getTekstBericht() {
        return tekstBericht;
    }

    public void setTekstBericht(String tekstBericht) {
        this.tekstBericht = tekstBericht;
    }

    public Integer getAantalLangeStroomStoringenInAlleFases() {
        return aantalLangeStroomStoringenInAlleFases;
    }

    public void setAantalLangeStroomStoringenInAlleFases(Integer aantalLangeStroomStoringenInAlleFases) {
        this.aantalLangeStroomStoringenInAlleFases = aantalLangeStroomStoringenInAlleFases;
    }

    public List<LangeStroomStoring> getLangeStroomStoringen() {
        return langeStroomStoringen;
    }

    public void setLangeStroomStoringen(List<LangeStroomStoring> langeStroomStoringen) {
        this.langeStroomStoringen = langeStroomStoringen;
    }

    public String getTekstBerichtCodes() {
        return tekstBerichtCodes;
    }

    public void setTekstBerichtCodes(String tekstBerichtCodes) {
        this.tekstBerichtCodes = tekstBerichtCodes;
    }

    public String getMeterIdentificatieStroom() {
        return meterIdentificatieStroom;
    }

    public void setMeterIdentificatieStroom(String meterIdentificatieStroom) {
        this.meterIdentificatieStroom = meterIdentificatieStroom;
    }

    public String getMeterIdentificatieGas() {
        return meterIdentificatieGas;
    }

    public void setMeterIdentificatieGas(String meterIdentificatieGas) {
        this.meterIdentificatieGas = meterIdentificatieGas;
    }

    public String getTariefIndicatorStroom() {
        return tariefIndicatorStroom;
    }

    public void setTariefIndicatorStroom(String tariefIndicatorStroom) {
        this.tariefIndicatorStroom = tariefIndicatorStroom;
    }
}
