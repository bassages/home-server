package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Kosten;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface KostenRepository extends JpaRepository<Kosten, Long> {

    @Cacheable(cacheNames = "kostenInPeriod")
    @Query(value = "SELECT k FROM Kosten k WHERE (:van BETWEEN k.van AND k.totEnMet) OR (:tot BETWEEN k.van AND k.totEnMet)")
    List<Kosten> getKostenInPeriod(@Param("van") long van, @Param("totEnMet") long totEnMet);

}
