package nl.wiegman.home.energie;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface MeterstandRepository extends JpaRepository<Meterstand, Long> {

    // JPQL queries. Please note that BETWEEN in HQL is INCLUSIVE
    String MOST_RECENT_IN_PERIOD = "SELECT m FROM Meterstand m WHERE m.datumtijd = (SELECT MAX(datumtijd) from Meterstand m where m.datumtijd BETWEEN :van AND :totEnMet)";
    String OLDEST_IN_PERIOD = "SELECT m FROM Meterstand m WHERE m.datumtijd = (SELECT MIN(datumtijd) from Meterstand m where m.datumtijd BETWEEN :van AND :totEnMet)";
    String OLDEST = "SELECT m FROM Meterstand m WHERE m.datumtijd = (SELECT MIN(oldest.datumtijd) FROM Meterstand oldest)";

    @Query(value = OLDEST)
    Meterstand getOudste();

    @Query(value = MOST_RECENT_IN_PERIOD)
    Meterstand getMeestRecenteInPeriode(@Param("van") long van, @Param("totEnMet") long totEnMet);

    @Query(value = OLDEST_IN_PERIOD)
    Meterstand getOudsteInPeriode(@Param("van") long van, @Param("totEnMet") long totEnMet);

    List<Meterstand> findByDatumtijdBetween(long van, long totEnMet);
}
