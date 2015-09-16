package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import nl.wiegman.homecontrol.services.apimodel.AfvalInzameling;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

@Api(value=AfvalInzamelingService.SERVICE_PATH, description="Geeft informatie over de afval inzameling")
@Component
@Path(AfvalInzamelingService.SERVICE_PATH)
public class AfvalInzamelingService {
    public static final String SERVICE_PATH = "afvalinzameling";

    public static final Map<String, AfvalInzameling.AfvalType> CALENDAR_TO_SERVICE_TYPE_MAP = new HashMap<>();

    private static byte[] cachedCalendar = null;

    private final Logger logger = LoggerFactory.getLogger(AfvalInzamelingService.class);

    static {
        CALENDAR_TO_SERVICE_TYPE_MAP.put("Restafval wordt opgehaald", AfvalInzameling.AfvalType.REST);
        CALENDAR_TO_SERVICE_TYPE_MAP.put("GFT wordt opgehaald", AfvalInzameling.AfvalType.GFT);
        CALENDAR_TO_SERVICE_TYPE_MAP.put("Plastic verpakkingen of PMD wordt opgehaald", AfvalInzameling.AfvalType.PLASTIC);
        CALENDAR_TO_SERVICE_TYPE_MAP.put("Inzameling Sallcon wordt opgehaald", AfvalInzameling.AfvalType.SALLCON);
    }

    @ApiOperation(value = "Geeft de eerstvolgende afval inzameling(en) terug. Als er op de huidige datum inzameling(en) gepland zijn, dan worden deze terug gegeven.")
    @GET
    @Path("volgende")
    @Produces(MediaType.APPLICATION_JSON)
    public AfvalInzameling ophalenVolgendeAfvalInzameling() throws IOException, ParserException {
        logger.info("start ophalenVolgendeAfvalInzameling()");

        AfvalInzameling volgendeInzameling = new AfvalInzameling();

        Calendar calendar = getCalendar();
        if (calendar != null) {
            List<VEvent> firstEventsFromNow = getNextEvents(calendar);
            volgendeInzameling.setAfvalTypes(new ArrayList<>());
            for (VEvent event : firstEventsFromNow) {
                volgendeInzameling.setDatum(event.getStartDate().getDate().getTime());
                volgendeInzameling.getAfvalTypes().add(CALENDAR_TO_SERVICE_TYPE_MAP.get(event.getDescription().getValue()));
            }
        }

        logger.info("end ophalenVolgendeAfvalInzameling() result=" + ReflectionToStringBuilder.toString(volgendeInzameling));
        return volgendeInzameling;
    }

    private Calendar getCalendar() {
        Calendar result = null;
        if (cachedCalendar == null) {
            try {
                cachedCalendar = downloadLatestCalendar();
            } catch (IOException e) {
                logger.error("Unable to download the latest afvalkalendar", e);
            }
        }
        if (cachedCalendar != null) {
            CalendarBuilder builder = new CalendarBuilder();
            try {
                result = builder.build(new ByteArrayInputStream(cachedCalendar));
            } catch (IOException | ParserException e) {
                logger.error("Unable to parse the latest afvalkalendar");
            }
        } else {
            logger.error("Unable to download the latest afvalkalendar");
        }

        return result;
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

    private byte[] downloadLatestCalendar() throws IOException {

        byte[] result = null;

        // Use custom cookie store if necessary.
        CookieStore cookieStore = new BasicCookieStore();

        // Create global request configuration
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .setExpectContinueEnabled(true)
                .build();

        // Create an HttpClient with the given custom dependencies and configuration.
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();

        try {
            // Execution context can be customized locally.
            HttpClientContext context = HttpClientContext.create();
            // Contextual attributes set the local context level will take
            // precedence over those set at the client level.
            context.setCookieStore(cookieStore);

            HttpPost login = new HttpPost("http://kalender.afvalvrij.nl/Afvalkalender/login.php");
            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
            nvps.add(new BasicNameValuePair("postcode", "7425 RH"));
            nvps.add(new BasicNameValuePair("huisnummer", "71"));
            nvps.add(new BasicNameValuePair("toon", ""));
            login.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            CloseableHttpResponse response = httpclient.execute(login, context);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 302) {
                throw new IOException("Invalid statuscode (expected 302): " + statusCode);
            }

            HttpGet downloadIcs = new HttpGet("http://kalender.afvalvrij.nl/Afvalkalender/download_ical.php?p=7425%20RH&h=71&t=");
            response = httpclient.execute(downloadIcs, context);
            try {
                statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new IOException("Invalid statuscode (expected 200): " + statusCode);
                }
                result = EntityUtils.toByteArray(response.getEntity());
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
        return result;
    }

}
