package nl.homeserver.energie.slimmemeter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import nl.homeserver.energie.StroomTariefIndicator;
import nl.homeserver.energie.meterstand.Meterstand;
import nl.homeserver.energie.meterstand.MeterstandService;
import nl.homeserver.energie.opgenomenvermogen.OpgenomenVermogen;
import nl.homeserver.energie.opgenomenvermogen.OpgenomenVermogenService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create",
                                  "cache.warmup.on-application-start:false" })
public class SlimmeMeterControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .alwaysDo(print())
                .build();
    }

    @MockBean
    private OpgenomenVermogenService opgenomenVermogenService;
    @MockBean
    private MeterstandService meterstandService;

    @Captor
    private ArgumentCaptor<Meterstand> meterstandCaptor;
    @Captor
    private ArgumentCaptor<OpgenomenVermogen> opgenomenVermogenCaptor;

    @WithMockUser
    @Test
    public void whenPostValidRequestToMeterstandEndpointThenMeterstandAndOpgenomenVermogenSaved() throws Exception {
        final String content = """
                {"datumtijd":"2018-05-03T13:14:15","stroomOpgenomenVermogenInWatt":640,"stroomTarief1":12.422,"stroomTarief2":26.241,"gas":664.242,"stroomTariefIndicator":2}
                """;

        mockMvc.perform(post("/api/slimmemeter").contentType(MediaType.APPLICATION_JSON).content(content))
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