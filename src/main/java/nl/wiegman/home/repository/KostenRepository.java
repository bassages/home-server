package nl.wiegman.home.repository;

import nl.wiegman.home.model.api.Kosten;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface KostenRepository extends JpaRepository<Kosten, Long> {

    String CACHE_NAME_KOSTEN_IN_PERIOD = "kostenInPeriod";

    @Cacheable(cacheNames = CACHE_NAME_KOSTEN_IN_PERIOD)
    @Query(value = "SELECT k FROM Kosten k WHERE (:van BETWEEN k.van AND k.totEnMet) OR (:totEnMet BETWEEN k.van AND k.totEnMet) OR (k.van >= :van AND k.totEnMet <= :totEnMet) ORDER BY k.van")
    List<Kosten> getKostenInPeriod(@Param("van") long van, @Param("totEnMet") long totEnMet);
}
