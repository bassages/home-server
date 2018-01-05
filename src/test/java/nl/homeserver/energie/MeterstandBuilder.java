package nl.homeserver.energie;

import static nl.homeserver.DateTimeUtil.toMillisSinceEpoch;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MeterstandBuilder {

    private LocalDateTime datumtijd = LocalDateTime.now();
    private BigDecimal stroomTarief1 = new BigDecimal("0.000");
    private BigDecimal stroomTarief2 = new BigDecimal("0.000");
    private BigDecimal gas = new BigDecimal("0.000");

    public static MeterstandBuilder aMeterstand() {
        return new MeterstandBuilder();
    }

    public MeterstandBuilder withDateTime(LocalDateTime dateTime) {
        this.datumtijd = dateTime;
        return this;
    }

    public Meterstand build() {
        Meterstand meterstand = new Meterstand();
        meterstand.setDatumtijd(toMillisSinceEpoch(datumtijd));
        meterstand.setDateTime(datumtijd);
        meterstand.setStroomTarief1(stroomTarief1);
        meterstand.setStroomTarief2(stroomTarief2);
        meterstand.setGas(gas);
        return meterstand;
    }

    public MeterstandBuilder withStroomTarief1(BigDecimal stroomTarief1) {
        this.stroomTarief1 = stroomTarief1;
        return this;
    }

    public MeterstandBuilder withStroomTarief2(BigDecimal stroomTarief2) {
        this.stroomTarief1 = stroomTarief2;
        return this;
    }

    public MeterstandBuilder withGas(BigDecimal gas) {
        this.gas = gas;
        return this;
    }
}
