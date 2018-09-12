package nl.homeserver.mindergasnl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
class MindergasnlSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
