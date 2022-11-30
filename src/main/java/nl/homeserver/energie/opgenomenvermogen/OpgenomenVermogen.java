package nl.homeserver.energie.opgenomenvermogen;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nl.homeserver.energie.StroomTariefIndicator;

import javax.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    @Getter
    @Setter
    private int watt;

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
