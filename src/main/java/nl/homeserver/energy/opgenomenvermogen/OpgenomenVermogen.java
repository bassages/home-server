package nl.homeserver.energy.opgenomenvermogen;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nl.homeserver.energy.StroomTariefIndicator;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@ToString
@Entity
@Getter
@Setter
public class OpgenomenVermogen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private long id;

    @Column(nullable = false, unique = true)
    @Setter(AccessLevel.NONE)
    private LocalDateTime datumtijd;

    @Column
    @Setter(AccessLevel.NONE)
    private LocalDate datum;

    @NotNull
    private int activePowerTotalInWatts;

    @NotNull
    private int activePowerL1InWatts;

    @NotNull
    private int activePowerL2InWatts;

    @NotNull
    private int activePowerL3InWatts;

    @Transient
    private Integer voltageL1;

    @Transient
    private Integer voltageL2;

    @Transient
    private Integer voltageL3;

    @Transient
    private Integer instantaneousCurrentL1Ampere;

    @Transient
    private Integer instantaneousCurrentL2Ampere;

    @Transient
    private Integer instantaneousCurrentL3Ampere;

    @NotNull
    @Setter(AccessLevel.NONE)
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
