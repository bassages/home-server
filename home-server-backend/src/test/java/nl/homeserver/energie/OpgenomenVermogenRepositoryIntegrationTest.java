package nl.homeserver.energie;

import nl.homeserver.RepositoryIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import static java.time.Month.JANUARY;
import static nl.homeserver.energie.OpgenomenVermogenBuilder.aOpgenomenVermogen;
import static org.assertj.core.api.Assertions.assertThat;

public class OpgenomenVermogenRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    private OpgenomenVermogenRepository opgenomenVermogenRepository;

    @Test
    public void shouldFindDatesBeforeToDateWithMoreRowsThan() {
        final LocalDate toDate = LocalDate.of(2017, JANUARY, 12);
        entityManager.persist(aOpgenomenVermogen().withDatumTijd(toDate.atTime(0, 0, 0)).build());
        entityManager.persist(aOpgenomenVermogen().withDatumTijd(toDate.atTime(12, 0, 9)).build());
        entityManager.persist(aOpgenomenVermogen().withDatumTijd(toDate.atTime(12, 0, 10)).build());

        final LocalDate dayBeforeToDateWith3Records = toDate.minusDays(1);
        entityManager.persist(aOpgenomenVermogen().withDatumTijd(dayBeforeToDateWith3Records.atTime(0, 0, 0)).build());
        entityManager.persist(aOpgenomenVermogen().withDatumTijd(dayBeforeToDateWith3Records.atTime(12, 0, 9)).build());
        entityManager.persist(aOpgenomenVermogen().withDatumTijd(dayBeforeToDateWith3Records.atTime(12, 0, 10)).build());

        final LocalDate dayBeforeToDateWith2Records = toDate.minusDays(2);
        entityManager.persist(aOpgenomenVermogen().withDatumTijd(dayBeforeToDateWith2Records.atTime(12, 0, 8)).build());
        entityManager.persist(aOpgenomenVermogen().withDatumTijd(dayBeforeToDateWith2Records.atTime(12, 0, 1)).build());

        final List<Timestamp> datesWithMoreThan2RowsBeforeToDate = opgenomenVermogenRepository.findDatesBeforeToDateWithMoreRowsThan(toDate, 2);

        assertThat(datesWithMoreThan2RowsBeforeToDate).hasSize(1);
        assertThat(datesWithMoreThan2RowsBeforeToDate.get(0).toLocalDateTime().toLocalDate()).isEqualTo(dayBeforeToDateWith3Records);
    }
}