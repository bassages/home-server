package nl.homeserver.energy.meterreading;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nl.homeserver.energy.StroomTariefIndicator;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@ToString
@Entity
public class Meterstand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Setter
    private long id;

    @Column(nullable = false)
    @Getter
    private LocalDateTime dateTime;

    @Column
    @Getter
    private LocalDate date;

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

    public void setDateTime(@Nullable final LocalDateTime dateTime) {
        this.dateTime = dateTime;
        this.date = dateTime == null ? null : dateTime.toLocalDate();
    }
}
