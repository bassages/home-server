package nl.homeserver.energie;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface OpgenomenVermogenRepository extends JpaRepository<OpgenomenVermogen, Long> {

    @Query(value = "SELECT ov FROM OpgenomenVermogen ov WHERE ov.datumtijd >= :van AND ov.datumtijd < :tot ORDER BY ov.datumtijd")
    List<OpgenomenVermogen> getOpgenomenVermogen(@Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);

    @Query(value = "SELECT ov FROM OpgenomenVermogen ov WHERE ov.datumtijd = (SELECT MAX(mostrecent.datumtijd) FROM OpgenomenVermogen mostrecent)")
    OpgenomenVermogen getMeestRecente();

}
