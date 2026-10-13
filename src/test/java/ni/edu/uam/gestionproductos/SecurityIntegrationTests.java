package ni.edu.uam.gestionproductos;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JwtEncoder encoder;
    @Autowired AuthenticationManager authenticationManager;

    private String token(String rol, boolean vencido) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("prueba")
                .issuedAt(ahora.minusSeconds(600))
                .expiresAt(vencido ? ahora.minusSeconds(300) : ahora.plusSeconds(300))
                .claim("roles", List.of(rol))
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    @Test
    void autenticaUsuariosDelLaboratorioYRechazaClaveIncorrecta() {
        for (String[] usuario : new String[][]{
                {"admin", "Admin123!", "ROLE_ADMIN"},
                {"operador", "Operador123!", "ROLE_USER"}}) {
            var autenticado = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(usuario[0], usuario[1]));
            assertTrue(autenticado.isAuthenticated());
            assertTrue(autenticado.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(usuario[2])));
        }
        assertThrows(BadCredentialsException.class, () -> authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("admin", "incorrecta")));
    }

    @Test
    void productosRequierenAutenticacion() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isUnauthorized());
    }

    @Test
    void jwtUserYAdminPuedenConsultarSinCrearSesion() throws Exception {
        for (String rol : List.of("USER", "ADMIN")) {
            var resultado = mvc.perform(get("/api/productos")
                            .header("Authorization", "Bearer " + token(rol, false)))
                    .andExpect(status().isOk()).andReturn();
            assertNull(resultado.getRequest().getSession(false));
        }
        // La petición anterior no autentica la siguiente.
        mvc.perform(get("/api/productos")).andExpect(status().isUnauthorized());
    }

    @Test
    void userNoPuedeModificarProductos() throws Exception {
        String bearer = "Bearer " + token("USER", false);
        mvc.perform(post("/api/productos").header("Authorization", bearer))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/productos/1").header("Authorization", bearer))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/productos/1").header("Authorization", bearer))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeEliminarProductoTemporalConJwt() throws Exception {
        // Crear datos propios evita modificar productos del laboratorio.
        var categoria = new ni.edu.uam.gestionproductos.entity.Categoria();
        categoria.setNombre("Categoria seguridad");
        em.persist(categoria);
        var producto = new ni.edu.uam.gestionproductos.entity.Producto();
        producto.setCodigo("SEC-" + System.nanoTime());
        producto.setNombre("Producto seguridad");
        producto.setCategoria(categoria);
        producto.setPrecioVenta(java.math.BigDecimal.ONE);
        em.persist(producto);
        em.flush();
        Integer id = producto.getId();
        mvc.perform(delete("/api/productos/{id}", id)
                        .header("Authorization", "Bearer " + token("ADMIN", false)))
                .andExpect(status().isNoContent());
        em.flush();
        em.clear();
        assertNull(em.find(ni.edu.uam.gestionproductos.entity.Producto.class, id));
    }

    @Autowired jakarta.persistence.EntityManager em;

    @Test
    void rechazaJwtVencidoFirmaAlteradaYRolNoAutorizado() throws Exception {
        mvc.perform(get("/api/productos")
                        .header("Authorization", "Bearer " + token("USER", true)))
                .andExpect(status().isUnauthorized());
        String valido = token("USER", false);
        int firma = valido.lastIndexOf('.') + 1;
        String alterado = valido.substring(0, firma)
                + (valido.charAt(firma) == 'A' ? "B" : "A") + valido.substring(firma + 1);
        mvc.perform(get("/api/productos").header("Authorization", "Bearer " + alterado))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/productos")
                        .header("Authorization", "Bearer " + token("OTRO", false)))
                .andExpect(status().isForbidden());
    }
}
