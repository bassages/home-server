package nl.wiegman.home.repository;

import nl.wiegman.home.model.Klimaat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Transactional
public interface KlimaatRepo extends JpaRepository<Klimaat, Long> {

    String MOST_RECENT = "SELECT k FROM KlimaatD k WHERE k.datumtijd = (SELECT MAX(mostrecent.datumtijd) FROM KlimaatD mostrecent)";

    List<Klimaat> findByDatumtijdBetweenOrderByDatumtijd(@Param("van") Date van, @Param("tot") Date tot);

    @Query(value = MOST_RECENT)
    Klimaat getMostRecent();
}
