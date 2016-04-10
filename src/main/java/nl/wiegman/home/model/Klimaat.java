package nl.wiegman.home.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
public class Klimaat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    private long datumtijd;

    @Column(precision = 4, scale = 2)
    private BigDecimal temperatuur;

    @Column(precision = 4, scale = 1)
    private BigDecimal luchtvochtigheid;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BigDecimal getTemperatuur() {
        return temperatuur;
    }

    public void setTemperatuur(BigDecimal temperatuur) {
        this.temperatuur = temperatuur;
    }

    public BigDecimal getLuchtvochtigheid() {
        return luchtvochtigheid;
    }

    public void setLuchtvochtigheid(BigDecimal luchtvochtigheid) {
        this.luchtvochtigheid = luchtvochtigheid;
    }

    public long getDatumtijd() {
        return datumtijd;
    }

    public void setDatumtijd(long datumtijd) {
        this.datumtijd = datumtijd;
    }
}
