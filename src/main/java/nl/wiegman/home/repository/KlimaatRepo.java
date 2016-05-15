package nl.wiegman.home.repository;

import nl.wiegman.home.model.Klimaat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface KlimaatRepo extends JpaRepository<Klimaat, Long> {

    // JPQL queries
    String ALL_IN_PERIOD_SORTED = "SELECT k FROM Klimaat k WHERE k.datumtijd >= :van AND k.datumtijd < :tot ORDER BY k.datumtijd";
    String MOST_RECENT = "SELECT k FROM Klimaat k WHERE k.datumtijd = (SELECT MAX(mostrecent.datumtijd) FROM Klimaat mostrecent)";

    @Query(value = ALL_IN_PERIOD_SORTED)
    List<Klimaat> getKlimaat(@Param("van") long van, @Param("tot") long tot);

    @Query(value = MOST_RECENT)
    Klimaat getMeestRecente();
}
