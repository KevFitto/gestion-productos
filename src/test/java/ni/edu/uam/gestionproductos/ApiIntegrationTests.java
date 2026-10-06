package ni.edu.uam.gestionproductos;

import ni.edu.uam.gestionproductos.entity.*;
import ni.edu.uam.gestionproductos.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired CategoriaRepository categorias;
    @Autowired ProveedorRepository proveedores;

    @Test
    void crearYConsultarProductoConRelaciones() throws Exception {
        Categoria categoria = new Categoria();
        categoria.setNombre("Categoría de prueba");
        categoria = categorias.saveAndFlush(categoria);
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Proveedor de prueba");
        proveedor.setTelefono("2222-3333");
        proveedor.setCorreo("prueba@example.com");
        proveedor = proveedores.saveAndFlush(proveedor);
        String codigo = "TEST-" + System.nanoTime();
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content("""
                {"codigo":"%s","nombre":"Laptop","categoria":{"id":%d},
                 "proveedor":{"id":%d},"precioVenta":850.00,"existencia":10,
                 "descripcion":"Equipo de prueba"}
                """.formatted(codigo, categoria.getId(), proveedor.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoria.nombre").value("Categoría de prueba"))
                .andExpect(jsonPath("$.proveedor.nombre").value("Proveedor de prueba"))
                .andExpect(jsonPath("$.descripcion").value("Equipo de prueba"));
        mvc.perform(get("/api/productos")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == '%s')].proveedor.correo".formatted(codigo))
                        .value(org.hamcrest.Matchers.hasItem("prueba@example.com")));
    }

    @Test
    void rechazarProductoInvalido() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"\",\"nombre\":\"\",\"precioVenta\":-1,\"existencia\":-2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazarCategoriaInexistente() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"codigo":"SIN-CAT","nombre":"Prueba","categoria":{"id":-1},
                         "precioVenta":10,"existencia":1}
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearCategoriasYProveedores() throws Exception {
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Accesorios\",\"activa\":true}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber());
        mvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"Distribuidor","telefono":"2222-3333",
                         "correo":"ventas@example.com","activo":true}
                        """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber());
        mvc.perform(get("/api/categorias")).andExpect(status().isOk());
        mvc.perform(get("/api/proveedores")).andExpect(status().isOk());
    }
}
