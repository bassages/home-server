package nl.homeserver.energie.opgenomenvermogen;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import nl.homeserver.energie.StroomTariefIndicator;

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
