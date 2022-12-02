package nl.homeserver.energy.mindergasnl;

import jakarta.persistence.*;
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
