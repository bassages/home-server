package nl.homeserver.energy.opgenomenvermogen;

import nl.homeserver.RepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static java.time.Month.JANUARY;
import static nl.homeserver.energy.opgenomenvermogen.OpgenomenVermogenBuilder.aOpgenomenVermogen;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class OpgenomenVermogenRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    OpgenomenVermogenRepository opgenomenVermogenRepository;

    @Test
    void shouldFindDatesBeforeToDateWithMoreRowsThan() {
        final LocalDate fromDate = LocalDate.of(2017, JANUARY, 10);
        entityManager.persist(aOpgenomenVermogen().withDatumTijd(fromDate.minusDays(1).atTime(0, 0, 0)).build());
        entityManager.persist(aOpgenomenVermogen().withDatumTijd(fromDate.minusDays(1).atTime(12, 0, 9)).build());
        entityManager.persist(aOpgenomenVermogen().withDatumTijd(fromDate.minusDays(1).atTime(12, 0, 10)).build());

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

        final List<Date> datesWithMoreThan2RowsBeforeToDate = opgenomenVermogenRepository.findDatesBeforeToDateWithMoreRowsThan(fromDate, toDate, 2);

        assertThat(datesWithMoreThan2RowsBeforeToDate)
                .singleElement()
                .extracting(Date::toLocalDate)
                .isEqualTo(dayBeforeToDateWith3Records);
    }

    @Test
    void shouldFindMostCommonWattInPeriod() {
        final LocalDate day = LocalDate.of(2017, JANUARY, 10);

        entityManager.persist(aOpgenomenVermogen().withWatt(2).withDatumTijd(day.atStartOfDay()).build());
        entityManager.persist(aOpgenomenVermogen().withWatt(10).withDatumTijd(day.atStartOfDay().plusMinutes(1)).build());
        entityManager.persist(aOpgenomenVermogen().withWatt(10).withDatumTijd(day.atStartOfDay().plusMinutes(2)).build());

        // Day after period should not be considered
        entityManager.persist(aOpgenomenVermogen().withWatt(10).withDatumTijd(day.plusDays(1).atStartOfDay().plusMinutes(1)).build());
        entityManager.persist(aOpgenomenVermogen().withWatt(10).withDatumTijd(day.plusDays(1).atStartOfDay().plusMinutes(2)).build());
        entityManager.persist(aOpgenomenVermogen().withWatt(10).withDatumTijd(day.plusDays(1).atStartOfDay().plusMinutes(3)).build());

        final Integer mostCommonWattInPeriod = opgenomenVermogenRepository.findMostCommonWattInPeriod(day.atStartOfDay(), day.plusDays(1).atStartOfDay());

        assertThat(mostCommonWattInPeriod).isEqualTo(10);
    }

    @Test
    void shouldCountNumberOfRecordsInPeriod() {
        final LocalDate day = LocalDate.of(2017, JANUARY, 10);

        entityManager.persist(aOpgenomenVermogen().withDatumTijd(day.atStartOfDay()).build());
        entityManager.persist(aOpgenomenVermogen().withDatumTijd(day.atStartOfDay().plusMinutes(1)).build());

        // Day after period should not be considered
        entityManager.persist(aOpgenomenVermogen().withDatumTijd(day.plusDays(1).atStartOfDay()).build());

        final long numberOfRecordsInPeriod = opgenomenVermogenRepository.countNumberOfRecordsInPeriod(
                day.atStartOfDay(), day.plusDays(1).atStartOfDay());

        assertThat(numberOfRecordsInPeriod).isEqualTo(2);
    }

    @Test
    void shouldGetNumberOfRecordsInRange() {
        final LocalDate day = LocalDate.of(2017, JANUARY, 10);

        entityManager.persist(aOpgenomenVermogen().withWatt(9).withDatumTijd(day.atStartOfDay()).build());

        entityManager.persist(aOpgenomenVermogen().withWatt(10).withDatumTijd(day.atStartOfDay().plusMinutes(1)).build());
        entityManager.persist(aOpgenomenVermogen().withWatt(11).withDatumTijd(day.atStartOfDay().plusMinutes(2)).build());
        entityManager.persist(aOpgenomenVermogen().withWatt(11).withDatumTijd(day.atStartOfDay().plusMinutes(3)).build());
        entityManager.persist(aOpgenomenVermogen().withWatt(19).withDatumTijd(day.atStartOfDay().plusMinutes(4)).build());

        entityManager.persist(aOpgenomenVermogen().withWatt(20).withDatumTijd(day.atStartOfDay().plusMinutes(5)).build());

        entityManager.persist(aOpgenomenVermogen().withDatumTijd(day.plusDays(1).atStartOfDay()).build());

        final List<NumberOfRecordsPerWatt> numberOfRecordsPerWatt = opgenomenVermogenRepository.numberOfRecordsInRange(
                day.atStartOfDay(), day.plusDays(1).atStartOfDay(), 10, 20);

        assertThat(numberOfRecordsPerWatt).extracting(NumberOfRecordsPerWatt::getNumberOfRecords,
                                                      NumberOfRecordsPerWatt::getWatt)
                                          .containsExactlyInAnyOrder(tuple(1L, 10L),
                                                                     tuple(2L, 11L),
                                                                     tuple(1L, 19L));
    }
}
