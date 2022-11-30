package nl.homeserver.energie.mindergasnl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

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
