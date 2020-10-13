package nl.homeserver.energie.meterstand;

import static java.time.Month.JANUARY;
import static nl.homeserver.energie.meterstand.MeterstandBuilder.aMeterstand;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import nl.homeserver.RepositoryIntegrationTest;

public class MeterstandRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    private MeterstandRepository meterstandRepository;

    @Test
    public void shouldFindDatesBeforeToDateWithMoreRowsThan() {
        final LocalDate fromDate = LocalDate.of(2017, JANUARY, 3);
        entityManager.persist(aMeterstand().withDateTime(fromDate.minusDays(1).atTime(0, 0, 0)).build());
        entityManager.persist(aMeterstand().withDateTime(fromDate.minusDays(1).atTime(12, 0, 9)).build());
        entityManager.persist(aMeterstand().withDateTime(fromDate.minusDays(1).atTime(12, 0, 10)).build());

        final LocalDate toDate = LocalDate.of(2017, JANUARY, 12);
        entityManager.persist(aMeterstand().withDateTime(toDate.atTime(0, 0, 0)).build());
        entityManager.persist(aMeterstand().withDateTime(toDate.atTime(12, 0, 9)).build());
        entityManager.persist(aMeterstand().withDateTime(toDate.atTime(12, 0, 10)).build());

        final LocalDate dayBeforeToDateWith3Records = toDate.minusDays(1);
        entityManager.persist(aMeterstand().withDateTime(dayBeforeToDateWith3Records.atTime(0, 0, 0)).build());
        entityManager.persist(aMeterstand().withDateTime(dayBeforeToDateWith3Records.atTime(12, 0, 9)).build());
        entityManager.persist(aMeterstand().withDateTime(dayBeforeToDateWith3Records.atTime(12, 0, 10)).build());

        final LocalDate dayBeforeToDateWith2Records = toDate.minusDays(2);
        entityManager.persist(aMeterstand().withDateTime(dayBeforeToDateWith2Records.atTime(12, 0, 8)).build());
        entityManager.persist(aMeterstand().withDateTime(dayBeforeToDateWith2Records.atTime(12, 0, 1)).build());

        final List<Timestamp> datesWithMoreThan2RowsBeforeToDate = meterstandRepository.findDatesBeforeToDateWithMoreRowsThan(fromDate, toDate, 2);

        assertThat(datesWithMoreThan2RowsBeforeToDate).hasSize(1);
        assertThat(datesWithMoreThan2RowsBeforeToDate.get(0).toLocalDateTime().toLocalDate()).isEqualTo(dayBeforeToDateWith3Records);
    }

    @Test
    public void shouldFindOldestInPeriod() {
        final LocalDateTime fromDateTime = LocalDate.of(2017, JANUARY, 3).atTime(0, 0, 0);

        entityManager.persist(aMeterstand().withDateTime(fromDateTime).build());
        entityManager.persist(aMeterstand().withDateTime(fromDateTime.plusDays(1)).build());

        final Meterstand oldestInPeriod = aMeterstand().withDateTime(fromDateTime.plusDays(2)).build();
        entityManager.persist(oldestInPeriod);

        entityManager.persist(aMeterstand().withDateTime(fromDateTime.plusDays(3)).build());
        entityManager.persist(aMeterstand().withDateTime(fromDateTime.plusDays(4)).build());

        final Meterstand actual = meterstandRepository.getOldestInPeriod(oldestInPeriod.getDateTime(), fromDateTime.plusYears(1));
        assertThat(actual).isEqualTo(oldestInPeriod);
    }
}
