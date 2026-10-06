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
    @Autowired ProductoRepository productos;

    @Test
    void listarYBuscarProductoConRelaciones() throws Exception {
        Categoria categoria = new Categoria();
        categoria.setNombre("Categoría de prueba");
        categoria = categorias.saveAndFlush(categoria);
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Proveedor de prueba");
        proveedor.setTelefono("2222-3333");
        proveedor.setCorreo("prueba@example.com");
        proveedor = proveedores.saveAndFlush(proveedor);
        String codigo = "TEST-" + System.nanoTime();
        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre("Laptop");
        producto.setCategoria(categoria);
        producto.setProveedor(proveedor);
        producto.setPrecioVenta(new java.math.BigDecimal("850.00"));
        producto.setExistencia(10);
        producto.setDescripcion("Equipo de prueba");
        producto = productos.saveAndFlush(producto);
        mvc.perform(get("/api/productos/{id}", producto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(producto.getId()))
                .andExpect(jsonPath("$.categoria.nombre").value("Categoría de prueba"))
                .andExpect(jsonPath("$.proveedor.nombre").value("Proveedor de prueba"))
                .andExpect(jsonPath("$.descripcion").value("Equipo de prueba"));
        mvc.perform(get("/api/productos")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == '%s')].proveedor.correo".formatted(codigo))
                        .value(org.hamcrest.Matchers.hasItem("prueba@example.com")));
    }

    @Test
    void buscarProductoInexistente() throws Exception {
        mvc.perform(get("/api/productos/-1"))
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
