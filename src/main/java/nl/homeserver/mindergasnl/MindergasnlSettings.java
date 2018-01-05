package nl.homeserver.mindergasnl;

import static javax.persistence.GenerationType.AUTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class MindergasnlSettings {

    @Id
    @GeneratedValue(strategy = AUTO)
    private long id;

    @Column(nullable = false)
    private boolean automatischUploaden;

    @Column
    private String authenticatietoken;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isAutomatischUploaden() {
        return automatischUploaden;
    }

    public void setAutomatischUploaden(boolean automatischUploaden) {
        this.automatischUploaden = automatischUploaden;
    }

    public String getAuthenticatietoken() {
        return authenticatietoken;
    }

    public void setAuthenticatietoken(String authenticatietoken) {
        this.authenticatietoken = authenticatietoken;
    }
}
