package nl.homeserver.energie.opgenomenvermogen;

import lombok.AllArgsConstructor;
import nl.homeserver.DatePeriod;
import nl.homeserver.DateTimePeriod;
import nl.homeserver.energie.StroomTariefIndicator;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.LongStream;

import static java.time.LocalDateTime.from;
import static java.util.Comparator.comparingInt;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static nl.homeserver.DateTimeUtil.toMillisSinceEpoch;

@Service
@AllArgsConstructor
public class OpgenomenVermogenService {

    static final String CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY = "opgenomenVermogenHistory";

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
                                 .max(comparingInt(OpgenomenVermogen::getWatt))
                                 .map(o -> this.mapToOpgenomenVermogen(o, period))
                                 .orElse(this.mapToEmptyOpgenomenVermogen(period.getFromDateTime()));
    }

    private OpgenomenVermogen mapToOpgenomenVermogen(final OpgenomenVermogen opgenomenVermogen,
                                                     final DateTimePeriod period) {
        final OpgenomenVermogen result = new OpgenomenVermogen();
        result.setTariefIndicator(opgenomenVermogen.getTariefIndicator());
        result.setDatumtijd(period.getFromDateTime());
        result.setWatt(opgenomenVermogen.getWatt());
        return result;
    }

    private OpgenomenVermogen mapToEmptyOpgenomenVermogen(final LocalDateTime datumtijd) {
        final OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setDatumtijd(datumtijd);
        opgenomenVermogen.setTariefIndicator(StroomTariefIndicator.ONBEKEND);
        opgenomenVermogen.setWatt(0);
        return opgenomenVermogen;
    }
}
