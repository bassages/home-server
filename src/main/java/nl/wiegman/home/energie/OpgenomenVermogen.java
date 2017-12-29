package nl.wiegman.home.energie;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Bevat het opgenomen vermogen op een bepaald moment in tijd.
 */
@Entity
public class OpgenomenVermogen {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    private LocalDateTime datumtijd;

    @NotNull
    private int watt;

    @NotNull
    private short tariefIndicator;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getDatumtijd() {
        return datumtijd;
    }

    public void setDatumtijd(LocalDateTime datumTijd) {
        this.datumtijd = datumTijd;
    }

    public void setWatt(int watt) {
        this.watt = watt;
    }

    public int getWatt() {
        return watt;
    }

    public StroomTariefIndicator getTariefIndicator() {
        return StroomTariefIndicator.byId(this.tariefIndicator);
    }

    public void setTariefIndicator(StroomTariefIndicator tariefIndicator) {
        this.tariefIndicator = tariefIndicator.getId();
    }

}
