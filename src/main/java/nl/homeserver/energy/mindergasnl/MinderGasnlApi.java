package nl.homeserver.energy.mindergasnl;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.PostExchange;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

interface MinderGasnlApi {
    String HEADER_NAME_AUTH_TOKEN = "AUTH-TOKEN";

    @PostExchange(url = "/meter_readings", contentType = APPLICATION_JSON_VALUE)
    ResponseEntity<Void> meterReading(@RequestHeader(HEADER_NAME_AUTH_TOKEN) String authToken,
                                      @RequestBody MinderGasnlMeterReading meterReading);

    record MinderGasnlMeterReading(
            @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd")
            LocalDate date,
            BigDecimal reading) { }
}
