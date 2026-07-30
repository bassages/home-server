package nl.homeserver.energy.opgenomenvermogen;

import lombok.RequiredArgsConstructor;
import nl.homeserver.DatePeriod;
import nl.homeserver.DateTimePeriod;
import nl.homeserver.energy.StroomTariefIndicator;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.LongStream;

import static java.time.LocalDateTime.from;
import static java.util.Comparator.comparingInt;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static nl.homeserver.DateTimeUtil.toMillisSinceEpoch;

@Service
@RequiredArgsConstructor
public class OpgenomenVermogenService {

    static final String TOPIC = "/topic/opgenomen-vermogen";

    private final OpgenomenVermogenRepository opgenomenVermogenRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @SuppressWarnings("UnusedReturnValue")
    public OpgenomenVermogen save(final OpgenomenVermogen opgenomenVermogen) {
        final OpgenomenVermogen savedOpgenomenVermogen = opgenomenVermogenRepository.save(opgenomenVermogen);
        messagingTemplate.convertAndSend(TOPIC, savedOpgenomenVermogen);
        return savedOpgenomenVermogen;
    }

    OpgenomenVermogen getMostRecent() {
        return opgenomenVermogenRepository.getMostRecent();
    }

    @Cacheable(cacheNames = CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY)
    public List<OpgenomenVermogen> getPotentiallyCachedHistory(final DatePeriod period, final Duration subPeriodDuration) {
        return getHistory(period, subPeriodDuration);
    }

    List<OpgenomenVermogen> getHistory(final DatePeriod period, final Duration subPeriodDuration) {
        final DateTimePeriod dateTimePeriod = period.toDateTimePeriod();

        final List<OpgenomenVermogen> opgenomenVermogenInPeriod = opgenomenVermogenRepository.getOpgenomenVermogen(
                dateTimePeriod.getFromDateTime(), dateTimePeriod.getToDateTime());

        final long subPeriodLengthInMillis = subPeriodDuration.getSeconds() * 1000;
        final long nrOfSubPeriodsInPeriod = (toMillisSinceEpoch(dateTimePeriod.getToDateTime()) - toMillisSinceEpoch(dateTimePeriod.getFromDateTime())) / subPeriodLengthInMillis;

        return LongStream.rangeClosed(0, nrOfSubPeriodsInPeriod)
                         .boxed()
                         .map(periodNumber -> this.toSubPeriod(dateTimePeriod.getFromDateTime(), periodNumber, subPeriodDuration))
                         .map(subPeriod -> this.getMaxOpgenomenVermogenInPeriode(opgenomenVermogenInPeriod, subPeriod))
                         .toList();
    }

    private DateTimePeriod toSubPeriod(final LocalDateTime from, final long periodNumber, final Duration subPeriodDuration) {
        final Duration durationUntilStartOfSubPeriod = subPeriodDuration.multipliedBy(periodNumber);
        final LocalDateTime subFrom = from(durationUntilStartOfSubPeriod.addTo(from));
        final LocalDateTime subTo = from(subPeriodDuration.addTo(subFrom));
        return aPeriodWithToDateTime(subFrom, subTo);
    }

    private OpgenomenVermogen getMaxOpgenomenVermogenInPeriode(final List<OpgenomenVermogen> opgenomenVermogens,
                                                               final DateTimePeriod period) {
        return opgenomenVermogens.stream()
                                 .filter(opgenomenVermogen -> period.isWithinPeriod(opgenomenVermogen.getDatumtijd()))
                                 .max(comparingInt(OpgenomenVermogen::getActivePowerTotalInWatts))
                                 .map(o -> this.mapToOpgenomenVermogen(o, period))
                                 .orElse(this.mapToEmptyOpgenomenVermogen(period.getFromDateTime()));
    }

    private OpgenomenVermogen mapToOpgenomenVermogen(final OpgenomenVermogen opgenomenVermogen,
                                                     final DateTimePeriod period) {
        final OpgenomenVermogen result = new OpgenomenVermogen();
        result.setTariefIndicator(opgenomenVermogen.getTariefIndicator());
        result.setDatumtijd(period.getFromDateTime());
        result.setActivePowerTotalInWatts(opgenomenVermogen.getActivePowerTotalInWatts());
        result.setActivePowerL1InWatts(opgenomenVermogen.getActivePowerL1InWatts());
        result.setActivePowerL2InWatts(opgenomenVermogen.getActivePowerL2InWatts());
        result.setActivePowerL3InWatts(opgenomenVermogen.getActivePowerL3InWatts());
        result.setVoltageL1(opgenomenVermogen.getVoltageL1());
        result.setVoltageL2(opgenomenVermogen.getVoltageL2());
        result.setVoltageL3(opgenomenVermogen.getVoltageL3());
        return result;
    }

    private OpgenomenVermogen mapToEmptyOpgenomenVermogen(final LocalDateTime datumtijd) {
        final OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setDatumtijd(datumtijd);
        opgenomenVermogen.setTariefIndicator(StroomTariefIndicator.ONBEKEND);
        opgenomenVermogen.setActivePowerTotalInWatts(0);
        opgenomenVermogen.setActivePowerL1InWatts(0);
        opgenomenVermogen.setActivePowerL2InWatts(0);
        opgenomenVermogen.setActivePowerL3InWatts(0);
        return opgenomenVermogen;
    }
}
