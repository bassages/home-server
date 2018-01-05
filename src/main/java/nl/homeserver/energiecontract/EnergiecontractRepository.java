package nl.homeserver.energiecontract;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface EnergiecontractRepository extends JpaRepository<Energiecontract, Long> {

    String CACHE_NAME_ENERGIECONTRACTEN_IN_PERIOD = "energiecontractenInPeriod";

    // JPQL queries
    String ALL_IN_PERIOD = "SELECT e FROM Energiecontract e WHERE (:van BETWEEN e.van AND e.totEnMet) OR (:totEnMet BETWEEN e.van AND e.totEnMet) OR (e.van >= :van AND e.totEnMet <= :totEnMet) ORDER BY e.van";

    @Cacheable(cacheNames = CACHE_NAME_ENERGIECONTRACTEN_IN_PERIOD)
    @Query(value = ALL_IN_PERIOD)
    List<Energiecontract> findAllInInPeriod(@Param("van") long van, @Param("totEnMet") long totEnMet);

    Energiecontract findFirstByVanLessThanEqualOrderByVanDesc(@Param("van") long van);
}
