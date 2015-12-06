package nl.wiegman.homecontrol.services.model.api;

import javax.persistence.*;

@Entity
@Table(name = "meterstand")
public class Meterstand {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private long datumtijd;

    private int stroomOpgenomenVermogenInWatt;

    private int stroomTarief1;

    private int stroomTarief2;

    private int gas;

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

    public int getStroomTarief1() {
        return stroomTarief1;
    }

    public void setStroomTarief1(int stroomTarief1) {
        this.stroomTarief1 = stroomTarief1;
    }

    public int getStroomTarief2() {
        return stroomTarief2;
    }

    public void setStroomTarief2(int stroomTarief2) {
        this.stroomTarief2 = stroomTarief2;
    }

    public int getGas() {
        return gas;
    }

    public void setGas(int gas) {
        this.gas = gas;
    }
}
