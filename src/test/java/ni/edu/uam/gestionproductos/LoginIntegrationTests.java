package ni.edu.uam.gestionproductos;

import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoginIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JwtDecoder decoder;

    @Test
    void saludoEsPublico() throws Exception {
        mvc.perform(get("/api/publico/saludo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("API NicaMarket operativa"));
    }

    @Test
    void loginEmiteJwtConClaimsYPermisosDeCadaUsuario() throws Exception {
        for (String[] usuario : new String[][]{
                {"admin", "Admin123!", "ADMIN"},
                {"operador", "Operador123!", "USER"}}) {
            var resultado = mvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"" + usuario[0]
                                    + "\",\"password\":\"" + usuario[1] + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andReturn();
            assertNull(resultado.getRequest().getSession(false));
            String token = JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
            var jwt = decoder.decode(token);
            assertEquals("HS256", jwt.getHeaders().get("alg"));
            assertEquals("nicamarket-laboratorio", jwt.getClaimAsString("iss"));
            assertEquals(usuario[0], jwt.getSubject());
            assertEquals(List.of(usuario[2]), jwt.getClaimAsStringList("roles"));
            assertEquals(Duration.ofMinutes(30),
                    Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
            mvc.perform(get("/api/productos").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
            // Un cuerpo ausente evita crear datos: ADMIN llega al controlador (400),
            // mientras que USER es rechazado por seguridad (403).
            mvc.perform(post("/api/productos").header("Authorization", "Bearer " + token))
                    .andExpect(status().is(usuario[2].equals("ADMIN") ? 400 : 403));
        }
        mvc.perform(get("/api/productos")).andExpect(status().isUnauthorized());
    }

    @Test
    void credencialesIncorrectasNoEmitenToken() throws Exception {
        for (String username : List.of("admin", "inexistente")) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"" + username + "\",\"password\":\"incorrecta\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.token").doesNotExist());
        }
    }
}
