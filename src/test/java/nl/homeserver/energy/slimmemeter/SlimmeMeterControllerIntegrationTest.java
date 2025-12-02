package nl.homeserver.energy.slimmemeter;

import static org.assertj.core.api.Assertions.assertThat;
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
@DirtiesContext
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
                {"datumtijd":"2018-05-03T13:14:15","stroomOpgenomenVermogenInWatt":640,"stroomTarief1":12.422,"stroomTarief2":26.241,"gas":664.242,"stroomTariefIndicator":2}
                """;

        mockMvc.perform(post("/api/slimmemeter")
                        .with(user("john@example.com"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content))
                .andExpect(status().isCreated());

        verify(meterstandService).save(meterstandCaptor.capture());
        verify(opgenomenVermogenService).save(opgenomenVermogenCaptor.capture());

        final Meterstand savedMeterstand = meterstandCaptor.getValue();
        assertThat(savedMeterstand.getDateTime()).isEqualTo(LocalDateTime.of(2018, 5, 3, 13, 14, 15));
        assertThat(savedMeterstand.getStroomTariefIndicator()).isEqualTo(StroomTariefIndicator.NORMAAL);
        assertThat(savedMeterstand.getStroomTarief1()).isEqualTo(new BigDecimal("12.422"));
        assertThat(savedMeterstand.getStroomTarief2()).isEqualTo(new BigDecimal("26.241"));
        assertThat(savedMeterstand.getGas()).isEqualTo(new BigDecimal("664.242"));

        final OpgenomenVermogen savedOpgenomenVermogen = opgenomenVermogenCaptor.getValue();
        assertThat(savedOpgenomenVermogen.getDatumtijd()).isEqualTo(LocalDateTime.of(2018, 5, 3, 13, 14, 15));
        assertThat(savedOpgenomenVermogen.getWatt()).isEqualTo(640);
        assertThat(savedOpgenomenVermogen.getTariefIndicator()).isEqualTo(StroomTariefIndicator.NORMAAL);
    }
}
