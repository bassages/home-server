package nl.homeserver.energie;

import static java.time.LocalDateTime.from;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static nl.homeserver.DateTimeUtil.toMillisSinceEpoch;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.LongStream;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import nl.homeserver.DatePeriod;
import nl.homeserver.DateTimePeriod;

@Service
public class OpgenomenVermogenService {

    public static final String CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY = "opgenomenVermogenHistory";

    public static final String TOPIC = "/topic/opgenomen-vermogen";

    private final OpgenomenVermogenRepository opgenomenVermogenRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public OpgenomenVermogenService(final OpgenomenVermogenRepository opgenomenVermogenRepository,
                                    final SimpMessagingTemplate messagingTemplate) {
        this.opgenomenVermogenRepository = opgenomenVermogenRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public OpgenomenVermogen save(OpgenomenVermogen opgenomenVermogen) {
        OpgenomenVermogen savedOpgenomenVermogen = opgenomenVermogenRepository.save(opgenomenVermogen);
        messagingTemplate.convertAndSend(TOPIC, savedOpgenomenVermogen);
        return savedOpgenomenVermogen;
    }

    public OpgenomenVermogen getMostRecent() {
        return opgenomenVermogenRepository.getMeestRecente();
    }

    @Cacheable(cacheNames = CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY)
    public List<OpgenomenVermogen> getPotentiallyCachedHistory(DatePeriod period, Duration subPeriodDuration) {
        return getHistory(period, subPeriodDuration);
    }

    public List<OpgenomenVermogen> getHistory(DatePeriod period, Duration subPeriodDuration) {
        DateTimePeriod dateTimePeriod = period.toDateTimePeriod();

        List<OpgenomenVermogen> opgenomenVermogenInPeriod = opgenomenVermogenRepository.getOpgenomenVermogen(
                dateTimePeriod.getFromDateTime(), dateTimePeriod.getToDateTime());

        long subPeriodLengthInMillis = subPeriodDuration.getSeconds() * 1000;
        long nrOfSubPeriodsInPeriod = (toMillisSinceEpoch(dateTimePeriod.getToDateTime()) - toMillisSinceEpoch(dateTimePeriod.getFromDateTime())) / subPeriodLengthInMillis;

        return LongStream.rangeClosed(0, nrOfSubPeriodsInPeriod)
                         .boxed()
                         .map(periodNumber -> this.toSubPeriod(dateTimePeriod.getFromDateTime(), periodNumber, subPeriodDuration))
                         .map(subPeriod -> this.getMaxOpgenomenVermogenInPeriode(opgenomenVermogenInPeriod, subPeriod))
                         .collect(toList());
    }

    private DateTimePeriod toSubPeriod(LocalDateTime from, long periodNumber, Duration subPeriodDuration) {
        Duration durationUntilStartOfSubPeriod = subPeriodDuration.multipliedBy(periodNumber);
        LocalDateTime subFrom = from(durationUntilStartOfSubPeriod.addTo(from));
        LocalDateTime subTo = from(subPeriodDuration.addTo(subFrom));
        return aPeriodWithToDateTime(subFrom, subTo);
    }

    private OpgenomenVermogen getMaxOpgenomenVermogenInPeriode(List<OpgenomenVermogen> opgenomenVermogens, DateTimePeriod period) {
        return opgenomenVermogens.stream()
                                 .filter(opgenomenVermogen -> period.isWithinPeriod(opgenomenVermogen.getDatumtijd()))
                                 .max(comparingInt(OpgenomenVermogen::getWatt))
                                 .map(o -> this.mapToOpgenomenVermogen(o, period))
                                 .orElse(this.mapToEmptyOpgenomenVermogen(period.getFromDateTime()));
    }

    private OpgenomenVermogen mapToOpgenomenVermogen(OpgenomenVermogen opgenomenVermogen, DateTimePeriod period) {
        OpgenomenVermogen result = new OpgenomenVermogen();
        result.setTariefIndicator(opgenomenVermogen.getTariefIndicator());
        result.setDatumtijd(period.getFromDateTime());
        result.setWatt(opgenomenVermogen.getWatt());
        return result;
    }

    private OpgenomenVermogen mapToEmptyOpgenomenVermogen(LocalDateTime datumtijd) {
        OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setDatumtijd(datumtijd);
        opgenomenVermogen.setTariefIndicator(StroomTariefIndicator.ONBEKEND);
        opgenomenVermogen.setWatt(0);
        return opgenomenVermogen;
    }
}
