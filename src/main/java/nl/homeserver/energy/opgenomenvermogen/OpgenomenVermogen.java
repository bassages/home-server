package nl.homeserver.energy.opgenomenvermogen;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nl.homeserver.energy.StroomTariefIndicator;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@ToString
@Entity
public class OpgenomenVermogen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Getter
    @Setter
    private long id;

    @Column(nullable = false, unique = true)
    @Getter
    private LocalDateTime datumtijd;

    @Column
    @Getter
    private LocalDate datum;

    @NotNull
    @Column(name = "active_power_total_in_watts")
    @Getter
    @Setter
    private int activePowerTotalInWatts;

    @NotNull
    @Getter
    @Setter
    private int activePowerL1InWatts;

    @NotNull
    @Getter
    @Setter
    private int activePowerL2InWatts;

    @NotNull
    @Getter
    @Setter
    private int activePowerL3InWatts;

    @NotNull
    private short tariefIndicator;

    public StroomTariefIndicator getTariefIndicator() {
        return StroomTariefIndicator.byId(this.tariefIndicator);
    }

    public void setTariefIndicator(final StroomTariefIndicator tariefIndicator) {
        this.tariefIndicator = tariefIndicator.getId();
    }

    public void setDatumtijd(@Nullable final LocalDateTime datumtijd) {
        this.datumtijd = datumtijd;
        this.datum = datumtijd == null ? null : datumtijd.toLocalDate();
    }
}
