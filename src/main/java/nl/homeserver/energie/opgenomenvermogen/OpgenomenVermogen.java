package nl.homeserver.energie.opgenomenvermogen;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nl.homeserver.energie.StroomTariefIndicator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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
    @Setter
    private LocalDateTime datumtijd;

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
}
