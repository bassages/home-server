package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import nl.wiegman.homecontrol.services.apimodel.AfvalInzameling;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.*;

@Api(value=AfvalInzamelingService.SERVICE_PATH, description="Geeft informatie over de afval inzameling")
@Component
@Path(AfvalInzamelingService.SERVICE_PATH)
public class AfvalInzamelingService {
    public static final String SERVICE_PATH = "afvalinzameling";

    public static final Map<String, AfvalInzameling.AfvalType> CALENDAR_TO_SERVICE_TYPE_MAP = new HashMap<>();

    static {
        CALENDAR_TO_SERVICE_TYPE_MAP.put("Restafval wordt opgehaald", AfvalInzameling.AfvalType.REST);
        CALENDAR_TO_SERVICE_TYPE_MAP.put("GFT wordt opgehaald", AfvalInzameling.AfvalType.GFT);
        CALENDAR_TO_SERVICE_TYPE_MAP.put("Plastic verpakkingen of PMD wordt opgehaald", AfvalInzameling.AfvalType.PLASTIC);
        CALENDAR_TO_SERVICE_TYPE_MAP.put("Inzameling Sallcon wordt opgehaald", AfvalInzameling.AfvalType.SALLCON);
    }

    @ApiOperation(value = "Geeft de eerstvolgende inzameling(en) terug. Als er op de huidige datum inzameling(en) gepland zijn, dan worden deze terug gegeven.")
    @GET
    @Path("volgende")
    @Produces(MediaType.APPLICATION_JSON)
    public AfvalInzameling next() throws IOException, ParserException {
        Calendar calendar = getCalendar();

        List<VEvent> firstEventsFromNow = getNextEvents(calendar);

        AfvalInzameling volgendeInzameling = new AfvalInzameling();
        volgendeInzameling.setAfvalTypes(new ArrayList<>());
        for (VEvent event : firstEventsFromNow) {
            volgendeInzameling.setDatum(Long.toString(event.getStartDate().getDate().getTime()));
            volgendeInzameling.getAfvalTypes().add(CALENDAR_TO_SERVICE_TYPE_MAP.get(event.getDescription().getValue()));
        }
        return volgendeInzameling;
    }

    private Calendar getCalendar() throws IOException, ParserException {
        Reader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/nl/wiegman/homecontrol/services/inzameling_7425RH_71.ics")));
        CalendarBuilder builder = new CalendarBuilder();
        return builder.build(reader);
    }

    private List<VEvent> getNextEvents(Calendar calendar) {
        Date firstDayFromTodayWithAtLeastOneEvent = findFirstDayFromTodayWithAtLeastOneEvent(calendar);
        return getAllEventsOnDay(firstDayFromTodayWithAtLeastOneEvent, calendar);
    }

    private List<VEvent> getAllEventsOnDay(Date day, Calendar calendar) {
        List<VEvent> eventsOnDay = new ArrayList<>();

        for (CalendarComponent calComp : calendar.getComponents()) {
            if (calComp instanceof VEvent) {
                VEvent event = (VEvent) calComp;
                net.fortuna.ical4j.model.Date eventDate = event.getStartDate().getDate();
                if (DateUtils.isSameDay(eventDate, day)) {
                    eventsOnDay.add(event);
                }
            }
        }
        return eventsOnDay;
    }

    private Date findFirstDayFromTodayWithAtLeastOneEvent(Calendar calendar) {
        Date today = new Date();
        today = DateUtils.truncate(today, java.util.Calendar.DATE);

        VEvent firstEventFromNow = null;

        for (CalendarComponent calComp : calendar.getComponents()) {
            if (calComp instanceof VEvent) {
                VEvent event = (VEvent) calComp;
                net.fortuna.ical4j.model.Date eventDate = event.getStartDate().getDate();
                if (eventDate.after(today)
                        && (firstEventFromNow==null || eventDate.before(firstEventFromNow.getStartDate().getDate()))) {
                    firstEventFromNow = event;
                }
            }
        }
        return firstEventFromNow.getStartDate().getDate();
    }
}
