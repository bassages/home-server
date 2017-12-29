package nl.wiegman.home.energie;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface OpgenomenVermogenRepository extends JpaRepository<OpgenomenVermogen, Long> {

    String ALL_IN_PERIOD_SORTED = "SELECT ov FROM OpgenomenVermogen ov WHERE ov.datumtijd >= :van AND ov.datumtijd < :tot ORDER BY ov.datumtijd";
    String MOST_RECENT = "SELECT ov FROM OpgenomenVermogen ov WHERE ov.datumtijd = (SELECT MAX(mostrecent.datumtijd) FROM OpgenomenVermogen mostrecent)";

    @Query(value = ALL_IN_PERIOD_SORTED)
    List<OpgenomenVermogen> getOpgenomenVermogen(@Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);

    @Query(value = MOST_RECENT)
    OpgenomenVermogen getMeestRecente();

}
