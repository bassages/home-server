package nl.wiegman.home.repository;

import nl.wiegman.home.model.Klimaat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

@Transactional
public interface KlimaatRepo extends JpaRepository<Klimaat, Long> {

    String MOST_RECENT = "SELECT * FROM klimaat WHERE datumtijd = (SELECT MAX(datumtijd) FROM klimaat)";

    @Query(value = MOST_RECENT, nativeQuery = true)
    Klimaat getMeestRecente();
}
