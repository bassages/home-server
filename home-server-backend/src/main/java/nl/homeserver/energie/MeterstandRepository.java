package nl.homeserver.energie;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface MeterstandRepository extends JpaRepository<Meterstand, Long> {

    // Please note that BETWEEN in HQL is INCLUSIVE

    @Query(value = "SELECT m FROM Meterstand m WHERE m.dateTime = (SELECT MAX(mostrecent.dateTime) FROM Meterstand mostrecent)")
    Meterstand getMostRecent();

    @Query(value = "SELECT m FROM Meterstand m WHERE m.dateTime = (SELECT MIN(oldest.dateTime) FROM Meterstand oldest)")
    Meterstand getOldest();

    @Query(value = "SELECT m FROM Meterstand m WHERE m.dateTime = (SELECT MAX(dateTime) from Meterstand m where m.dateTime BETWEEN :van AND :totEnMet)")
    Meterstand getMostRecentInPeriod(@Param("van") LocalDateTime start, @Param("totEnMet") LocalDateTime end);

    @Query(value = "SELECT m FROM Meterstand m WHERE m.dateTime = (SELECT MIN(dateTime) from Meterstand m where m.dateTime BETWEEN :van AND :totEnMet)")
    Meterstand getOldestInPeriod(@Param("van") LocalDateTime start, @Param("totEnMet") LocalDateTime end);

    List<Meterstand> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
