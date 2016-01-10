package nl.wiegman.homecontrol.services.repository;

import nl.wiegman.homecontrol.services.model.api.Kosten;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import javax.transaction.Transactional;
import java.util.List;

@RepositoryRestResource(path = "kosten", collectionResourceRel = "kosten")
@Transactional
public interface KostenRepository extends JpaRepository<Kosten, Long> {

    @Cacheable(cacheNames = "kostenInPeriod")
    @Query(value = "SELECT k FROM Kosten k WHERE (:van BETWEEN k.van AND k.totEnMet) OR (:totEnMet BETWEEN k.van AND k.totEnMet)")
    List<Kosten> getKostenInPeriod(@Param("van") long van, @Param("totEnMet") long totEnMet);

}
