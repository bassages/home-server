package nl.homeserver.energie.meterstand;

import nl.homeserver.energie.StroomTariefIndicator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SuppressWarnings({ "FieldMayBeFinal", "WeakerAccess", "CanBeFinal" })
public class MeterstandBuilder {

    private long id;
    private LocalDateTime datumtijd = LocalDateTime.now();
    private BigDecimal stroomTarief1 = new BigDecimal("0.000");
    private BigDecimal stroomTarief2 = new BigDecimal("0.000");
    private BigDecimal gas = new BigDecimal("0.000");
    private StroomTariefIndicator stroomTariefIndicator = StroomTariefIndicator.ONBEKEND;

    private MeterstandBuilder() {
        // Hide public constructor
    }

    public static MeterstandBuilder aMeterstand() {
        return new MeterstandBuilder();
    }

    public MeterstandBuilder withDateTime(final LocalDateTime dateTime) {
        this.datumtijd = dateTime;
        return this;
    }

    public MeterstandBuilder withStroomTarief1(final BigDecimal stroomTarief1) {
        this.stroomTarief1 = stroomTarief1;
        return this;
    }

    public MeterstandBuilder withStroomTarief2(final BigDecimal stroomTarief2) {
        this.stroomTarief1 = stroomTarief2;
        return this;
    }

    public MeterstandBuilder withGas(final BigDecimal gas) {
        this.gas = gas;
        return this;
    }

    public MeterstandBuilder withId(final long id) {
        this.id = id;
        return this;
    }

    public MeterstandBuilder withStroomTariefindicator(final StroomTariefIndicator stroomTariefIndicator) {
        this.stroomTariefIndicator = stroomTariefIndicator;
        return this;
    }

    public Meterstand build() {
        final Meterstand meterstand = new Meterstand();
        meterstand.setStroomTariefIndicator(stroomTariefIndicator);
        meterstand.setDateTime(datumtijd);
        meterstand.setId(id);
        meterstand.setStroomTarief1(stroomTarief1);
        meterstand.setStroomTarief2(stroomTarief2);
        meterstand.setGas(gas);
        return meterstand;
    }
}
