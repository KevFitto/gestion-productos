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
    @Autowired jakarta.persistence.EntityManager entityManager;

    @Test
    void actualizarProductoConDto() throws Exception {
        Categoria original = new Categoria();
        original.setNombre("Categoría original");
        original = categorias.saveAndFlush(original);
        Categoria nueva = new Categoria();
        nueva.setNombre("Categoría nueva");
        nueva = categorias.saveAndFlush(nueva);
        Producto producto = new Producto();
        producto.setCodigo("OLD-" + System.nanoTime());
        producto.setNombre("Teclado anterior");
        producto.setPrecioVenta(new java.math.BigDecimal("50.00"));
        producto.setExistencia(5);
        producto.setCategoria(original);
        producto.setDescripcion("Descripción existente");
        Integer id = productos.saveAndFlush(producto).getId();
        long cantidad = productos.count();
        String codigo = "UPD-" + System.nanoTime();

        mvc.perform(put("/api/productos/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"codigo":"%s","nombre":"Teclado actualizado",
                         "precioVenta":80.50,"existencia":15,"categoriaId":%d}
                        """.formatted(codigo, nueva.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        entityManager.flush();
        entityManager.clear();
        mvc.perform(get("/api/productos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.nombre").value("Teclado actualizado"))
                .andExpect(jsonPath("$.precioVenta").value(80.50))
                .andExpect(jsonPath("$.existencia").value(15))
                .andExpect(jsonPath("$.categoria.id").value(nueva.getId()))
                .andExpect(jsonPath("$.descripcion").value("Descripción existente"));
        org.junit.jupiter.api.Assertions.assertEquals(cantidad, productos.count());
    }

    @Test
    void actualizarProductoInexistente() throws Exception {
        mvc.perform(put("/api/productos/-1").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"codigo":"NO-EXISTE","nombre":"Prueba",
                         "precioVenta":10,"existencia":1,"categoriaId":-1}
                        """))
                .andExpect(status().isNotFound());
    }

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
    void registrarProductoConDto() throws Exception {
        Categoria categoria = new Categoria();
        categoria.setNombre("Teclados");
        categoria = categorias.saveAndFlush(categoria);
        String codigo = "DTO-" + System.nanoTime();

        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"codigo":"%s","nombre":"Teclado mecánico",
                         "precioVenta":75.50,"existencia":20,"categoriaId":%d}
                        """.formatted(codigo, categoria.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.nombre").value("Teclado mecánico"))
                .andExpect(jsonPath("$.precioVenta").value(75.50))
                .andExpect(jsonPath("$.existencia").value(20))
                .andExpect(jsonPath("$.categoria.id").value(categoria.getId()));

        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == '%s')].categoria.id".formatted(codigo))
                        .value(org.hamcrest.Matchers.hasItem(categoria.getId())));
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
