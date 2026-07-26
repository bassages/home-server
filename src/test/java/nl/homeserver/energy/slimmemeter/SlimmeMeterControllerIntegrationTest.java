package nl.homeserver.energy.slimmemeter;

import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import nl.homeserver.energy.StroomTariefIndicator;
import nl.homeserver.energy.meterreading.Meterstand;
import nl.homeserver.energy.meterreading.MeterstandService;
import nl.homeserver.energy.opgenomenvermogen.OpgenomenVermogen;
import nl.homeserver.energy.opgenomenvermogen.OpgenomenVermogenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create",
                                  "home-server.cache.warmup.on-application-start:false" })
class SlimmeMeterControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OpgenomenVermogenService opgenomenVermogenService;
    @MockitoBean
    MeterstandService meterstandService;

    @Captor
    ArgumentCaptor<Meterstand> meterstandCaptor;
    @Captor
    ArgumentCaptor<OpgenomenVermogen> opgenomenVermogenCaptor;

    @Test
    void whenPostValidRequestToMeterstandEndpointThenMeterstandAndOpgenomenVermogenSaved() throws Exception {
        final String content = """
                {"datumtijd":"2018-05-03T13:14:15","stroomOpgenomenVermogenInWatt":640,"directGeleverdVermogenL1InWatt":210,"directGeleverdVermogenL2InWatt":211,"directGeleverdVermogenL3InWatt":219,"stroomTarief1":12.422,"stroomTarief2":26.241,"gas":664.242,"stroomTariefIndicator":2,"meterIdentificatieStroom":"METER_ID_ELEC","meterIdentificatieGas":"METER_ID_GAS"}
                """;

        mockMvc.perform(post("/api/slimmemeter")
                        .with(user("john@example.com"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content))
                .andExpect(status().isCreated());

        verify(meterstandService).save(meterstandCaptor.capture());
        verify(opgenomenVermogenService).save(opgenomenVermogenCaptor.capture());

        final Meterstand savedMeterstand = meterstandCaptor.getValue();
        assertThat(savedMeterstand.getDateTime()).isEqualTo(LocalDateTime.of(2018, MAY, 3, 13, 14, 15));
        assertThat(savedMeterstand.getStroomTariefIndicator()).isEqualTo(StroomTariefIndicator.NORMAAL);
        assertThat(savedMeterstand.getStroomTarief1()).isEqualTo(new BigDecimal("12.422"));
        assertThat(savedMeterstand.getStroomTarief2()).isEqualTo(new BigDecimal("26.241"));
        assertThat(savedMeterstand.getGas()).isEqualTo(new BigDecimal("664.242"));
        assertThat(savedMeterstand.getMeterIdElectricity()).isEqualTo("METER_ID_ELEC");
        assertThat(savedMeterstand.getMeterIdGas()).isEqualTo("METER_ID_GAS");

        final OpgenomenVermogen savedOpgenomenVermogen = opgenomenVermogenCaptor.getValue();
        assertThat(savedOpgenomenVermogen.getDatumtijd()).isEqualTo(LocalDateTime.of(2018, MAY, 3, 13, 14, 15));
        assertThat(savedOpgenomenVermogen.getActivePowerTotalInWatts()).isEqualTo(640);
        assertThat(savedOpgenomenVermogen.getActivePowerL1InWatts()).isEqualTo(210);
        assertThat(savedOpgenomenVermogen.getActivePowerL2InWatts()).isEqualTo(211);
        assertThat(savedOpgenomenVermogen.getActivePowerL3InWatts()).isEqualTo(219);
        assertThat(savedOpgenomenVermogen.getTariefIndicator()).isEqualTo(StroomTariefIndicator.NORMAAL);
    }

    @Test
    void whenPostValidRequestTwiceWithinTenSecondsThenSecondReadingNotSaved() throws Exception {
        final String first = """
                {"datumtijd":"2018-05-03T13:14:15","stroomOpgenomenVermogenInWatt":640,"directGeleverdVermogenL1InWatt":210,"directGeleverdVermogenL2InWatt":211,"directGeleverdVermogenL3InWatt":219,"stroomTarief1":12.422,"stroomTarief2":26.241,"gas":664.242,"stroomTariefIndicator":2,"meterIdentificatieStroom":"METER_ID_ELEC","meterIdentificatieGas":"METER_ID_GAS"}
                """;
        final String second = """
                {"datumtijd":"2018-05-03T13:14:24","stroomOpgenomenVermogenInWatt":641,"directGeleverdVermogenL1InWatt":220,"directGeleverdVermogenL2InWatt":221,"directGeleverdVermogenL3InWatt":200,"stroomTarief1":12.423,"stroomTarief2":26.242,"gas":664.243,"stroomTariefIndicator":2,"meterIdentificatieStroom":"METER_ID_ELEC","meterIdentificatieGas":"METER_ID_GAS"}
                """;

        mockMvc.perform(post("/api/slimmemeter")
                        .with(user("john@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(first))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/slimmemeter")
                        .with(user("john@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(second))
                .andExpect(status().isCreated());

        verify(meterstandService, times(1)).save(any(Meterstand.class));
        verify(opgenomenVermogenService, times(1)).save(any(OpgenomenVermogen.class));
    }
}
