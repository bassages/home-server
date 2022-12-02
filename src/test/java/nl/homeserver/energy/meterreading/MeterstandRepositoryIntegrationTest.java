package nl.homeserver.energy.meterreading;

import nl.homeserver.RepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.Month.JANUARY;
import static nl.homeserver.energy.meterreading.MeterstandBuilder.aMeterstand;
import static org.assertj.core.api.Assertions.assertThat;

class MeterstandRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    MeterstandRepository meterstandRepository;

    @Test
    void shouldFindDatesBeforeToDateWithMoreRowsThan() {
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

        final List<Date> datesWithMoreThan2RowsBeforeToDate = meterstandRepository.findDatesBeforeToDateWithMoreRowsThan(fromDate, toDate, 2);

        assertThat(datesWithMoreThan2RowsBeforeToDate)
                .singleElement()
                .extracting(Date::toLocalDate)
                .isEqualTo(dayBeforeToDateWith3Records);
    }

    @Test
    void givenMultipleMeterstandInPeriodWhenFindOldestInPeriodThenOldestIsReturned() {
        // given
        final LocalDateTime fromDateTime = LocalDate.of(2017, JANUARY, 3).atTime(0, 0, 0);

        entityManager.persist(aMeterstand().withDateTime(fromDateTime).build());
        entityManager.persist(aMeterstand().withDateTime(fromDateTime.plusDays(1)).build());

        final Meterstand oldestInPeriod = aMeterstand().withDateTime(fromDateTime.plusDays(2)).build();
        entityManager.persist(oldestInPeriod);

        entityManager.persist(aMeterstand().withDateTime(fromDateTime.plusDays(3)).build());
        entityManager.persist(aMeterstand().withDateTime(fromDateTime.plusDays(4)).build());

        // when
        final Optional<Meterstand> actual = meterstandRepository.findOldestInPeriod(oldestInPeriod.getDateTime(), fromDateTime.plusYears(1));

        // then
        assertThat(actual).contains(oldestInPeriod);
    }

    @Test
    void givenMultipleMeterstandInPeriodWhenFindMostRecentInPeriodThenMostRecentIsReturned() {
        // given
        final LocalDateTime fromDateTime = LocalDate.of(2017, JANUARY, 3).atTime(0, 0, 0);

        entityManager.persist(aMeterstand().withDateTime(fromDateTime).build());
        entityManager.persist(aMeterstand().withDateTime(fromDateTime.plusDays(1)).build());
        entityManager.persist(aMeterstand().withDateTime(fromDateTime.plusDays(3)).build());
        final Meterstand mostRecent = aMeterstand().withDateTime(fromDateTime.plusDays(4)).build();
        entityManager.persist(mostRecent);

        // when
        final Optional<Meterstand> actual = meterstandRepository.findMostRecentInPeriod(fromDateTime, fromDateTime.plusYears(1));

        // then
        assertThat(actual).contains(mostRecent);
    }

    @Test
    void givenNoMeterstandInPeriodWhenFindMostRecentInPeriodThenEmptyOptionalIsReturned() {
        // given
        final LocalDateTime fromDateTime = LocalDate.of(2017, JANUARY, 3).atTime(0, 0, 0);

        // when
        final Optional<Meterstand> actual = meterstandRepository.findMostRecentInPeriod(fromDateTime, fromDateTime.plusYears(1));

        // then
        assertThat(actual).isEmpty();
    }
}
