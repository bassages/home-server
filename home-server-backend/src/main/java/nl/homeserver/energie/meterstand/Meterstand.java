package nl.homeserver.energie.meterstand;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import nl.homeserver.energie.StroomTariefIndicator;

@Entity
public class Meterstand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Setter
    private long id;

    @Column(nullable = false)
    @Getter
    @Setter
    private LocalDateTime dateTime;

    @Column(nullable = false, precision = 8, scale = 3)
    @Getter
    @Setter
    private BigDecimal stroomTarief1;

    @Column(nullable = false, precision = 8, scale = 3)
    @Getter
    @Setter
    private BigDecimal stroomTarief2;

    @Column(nullable = false, precision = 8, scale = 3)
    @Getter
    @Setter
    private BigDecimal gas;

    @Column(nullable = false, precision = 1)
    private short stroomTariefIndicator;

    public StroomTariefIndicator getStroomTariefIndicator() {
        return StroomTariefIndicator.byId(this.stroomTariefIndicator);
    }

    public void setStroomTariefIndicator(final StroomTariefIndicator stroomTariefIndicator) {
        this.stroomTariefIndicator = stroomTariefIndicator.getId();
    }
}
