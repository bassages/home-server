package nl.homeserver.energie.energycontract;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

interface EnergiecontractRepository extends JpaRepository<Energycontract, Long> {

    @Query(value = """
        SELECT e
          FROM Energycontract e
         WHERE (e.validFrom < :to) AND (e.validTo IS NULL OR e.validTo > :from)
      ORDER BY e.validFrom
      """)
    List<Energycontract> findValidInPeriod(@Param("from") final LocalDate from,
                                           @Param("to") final LocalDate to);

    Energycontract findFirstByValidFromLessThanEqualOrderByValidFromDesc(@Param("validFrom") LocalDate validFrom);
}
