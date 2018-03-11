package nl.homeserver.mindergasnl;

import static javax.persistence.GenerationType.AUTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
public class MindergasnlSettings {

    @Id
    @GeneratedValue(strategy = AUTO)
    private long id;

    @Column(nullable = false)
    @Getter
    @Setter
    private boolean automatischUploaden;

    @Column
    @Getter
    @Setter
    private String authenticatietoken;
}
