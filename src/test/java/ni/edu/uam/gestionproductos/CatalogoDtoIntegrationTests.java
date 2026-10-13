package ni.edu.uam.gestionproductos;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogoDtoIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired EntityManager entityManager;

    @Test
    void categoriaConservaEstadoPredeterminadoYPermiteInactiva() throws Exception {
        for (boolean activa : new boolean[]{true, false}) {
            String nombre = "Categoria DTO " + System.nanoTime();
            String estado = activa ? "" : ",\"activa\":false";
            mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nombre\":\"" + nombre + "\"" + estado + "}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.activa").value(activa));
            entityManager.flush();
            entityManager.clear();
            assertEquals(activa, entityManager.createQuery(
                    "select c.activa from Categoria c where c.nombre = :nombre", Boolean.class)
                    .setParameter("nombre", nombre).getSingleResult());
        }
    }

    @Test
    void proveedorPersisteSusDatosYRespetaEstado() throws Exception {
        for (boolean activo : new boolean[]{true, false}) {
            String nombre = "Proveedor DTO " + System.nanoTime();
            String estado = activo ? "" : ",\"activo\":false";
            mvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nombre\":\"" + nombre
                                    + "\",\"telefono\":\"88888888\",\"correo\":\"dto@example.com\"" + estado + "}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.telefono").value("88888888"))
                    .andExpect(jsonPath("$.correo").value("dto@example.com"))
                    .andExpect(jsonPath("$.activo").value(activo));
            entityManager.flush();
            entityManager.clear();
            var proveedor = entityManager.createQuery(
                    "select p from Proveedor p where p.nombre = :nombre",
                    ni.edu.uam.gestionproductos.entity.Proveedor.class)
                    .setParameter("nombre", nombre).getSingleResult();
            assertEquals(activo, proveedor.isActivo());
            assertEquals("88888888", proveedor.getTelefono());
            assertEquals("dto@example.com", proveedor.getCorreo());
        }
    }

    @Test
    void rechazaNombresVaciosYDemasiadoLargos() throws Exception {
        for (String ruta : new String[]{"categorias", "etiquetas", "proveedores"}) {
            int limite = ruta.equals("proveedores") ? 150 : 100;
            for (String nombre : new String[]{" ", "a".repeat(limite + 1)}) {
                mvc.perform(post("/api/" + ruta).contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nombre\":\"" + nombre
                                        + "\",\"telefono\":\"88888888\",\"correo\":\"dto@example.com\"}"))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @Test
    void rechazaProveedorSinContactoOConCorreoInvalido() throws Exception {
        for (String body : new String[]{
                "{\"nombre\":\"Proveedor\"}",
                "{\"nombre\":\"Proveedor\",\"telefono\":\"88888888\",\"correo\":\"invalido\"}"}) {
            mvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }
}
