package nl.homeserver.energiecontract;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface EnergiecontractRepository extends JpaRepository<Energiecontract, Long> {

    @Query(value = "   SELECT e "
                 + "     FROM Energiecontract e "
                 + "    WHERE (e.validFrom < :to) AND (e.validTo IS NULL OR e.validTo > :from) "
                 + " ORDER BY e.validFrom")
    List<Energiecontract> findValidInPeriod(@Param("from") final LocalDate from,
                                            @Param("to") final LocalDate to);

    Energiecontract findFirstByValidFromLessThanEqualOrderByValidFromDesc(@Param("validFrom") LocalDate validFrom);
}
