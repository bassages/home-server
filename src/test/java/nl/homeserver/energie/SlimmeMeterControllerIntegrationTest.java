package nl.homeserver.energie;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(SlimmeMeterController.class)
@TestPropertySource(properties = {
        "security.basic.enabled=false"
})
public class SlimmeMeterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpgenomenVermogenService opgenomenVermogenService;
    @MockBean
    private MeterstandService meterstandService;

    @Captor
    private ArgumentCaptor<Meterstand> meterstandCaptor;
    @Captor
    private ArgumentCaptor<OpgenomenVermogen> opgenomenVermogenCaptor;

    @Test
    public void whenPostValidRequestToMeterstandEndpointThenMeterstandAndOpgenomenVermogenSaved() throws Exception {
        String content = "{\"datumtijd\":\"2018-05-03T13:14:15\",\"stroomOpgenomenVermogenInWatt\":640,\"stroomTarief1\":12.422,\"stroomTarief2\":26.241,\"gas\":664.242,\"stroomTariefIndicator\":2}\n";;

        mockMvc.perform(post("/api/slimmemeter").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
               .andExpect(status().isCreated());

        verify(meterstandService).save(meterstandCaptor.capture());
        verify(opgenomenVermogenService).save(opgenomenVermogenCaptor.capture());

        Meterstand savedMeterstand = meterstandCaptor.getValue();
        assertThat(savedMeterstand.getDateTime()).isEqualTo(LocalDateTime.of(2018, 5, 3, 13, 14, 15));
        assertThat(savedMeterstand.getStroomTariefIndicator()).isEqualTo(StroomTariefIndicator.NORMAAL);
        assertThat(savedMeterstand.getStroomTarief1()).isEqualTo(new BigDecimal("12.422"));
        assertThat(savedMeterstand.getStroomTarief2()).isEqualTo(new BigDecimal("26.241"));
        assertThat(savedMeterstand.getGas()).isEqualTo(new BigDecimal("664.242"));

        OpgenomenVermogen savedOpgenomenVermogen = opgenomenVermogenCaptor.getValue();
        assertThat(savedOpgenomenVermogen.getDatumtijd()).isEqualTo(LocalDateTime.of(2018, 5, 3, 13, 14, 15));
        assertThat(savedOpgenomenVermogen.getWatt()).isEqualTo(640);
        assertThat(savedOpgenomenVermogen.getTariefIndicator()).isEqualTo(StroomTariefIndicator.NORMAAL);
    }
}