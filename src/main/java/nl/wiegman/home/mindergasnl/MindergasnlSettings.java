package nl.wiegman.home.mindergasnl;

import javax.persistence.*;

@Entity
public class MindergasnlSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
