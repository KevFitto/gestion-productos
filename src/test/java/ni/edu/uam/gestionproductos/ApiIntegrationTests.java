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
    void asociarEtiquetaPersisteSinDuplicar() throws Exception {
        Categoria categoria = new Categoria();
        categoria.setNombre("Categoría para etiquetas");
        categoria = categorias.saveAndFlush(categoria);
        Producto producto = new Producto();
        producto.setCodigo("TAG-" + System.nanoTime());
        producto.setNombre("Producto etiquetado");
        producto.setPrecioVenta(new java.math.BigDecimal("10.00"));
        producto.setCategoria(categoria);
        Integer productoId = productos.saveAndFlush(producto).getId();
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Oferta de prueba " + System.nanoTime());
        entityManager.persist(etiqueta);
        entityManager.flush();
        Integer etiquetaId = etiqueta.getId();
        entityManager.clear();

        for (int intento = 0; intento < 2; intento++) {
            mvc.perform(post("/api/productos/{productoId}/etiquetas/{etiquetaId}", productoId, etiquetaId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(productoId))
                    .andExpect(jsonPath("$.etiquetas", org.hamcrest.Matchers.hasSize(1)))
                    .andExpect(jsonPath("$.etiquetas[0].id").value(etiquetaId));
            entityManager.flush();
            entityManager.clear();
        }
        Number asociaciones = (Number) entityManager.createNativeQuery(
                "select count(*) from producto_etiqueta where producto_id = :producto and etiqueta_id = :etiqueta")
                .setParameter("producto", productoId)
                .setParameter("etiqueta", etiquetaId)
                .getSingleResult();
        org.junit.jupiter.api.Assertions.assertEquals(1L, asociaciones.longValue());
        mvc.perform(get("/api/productos/{id}", productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etiquetas[0].id").value(etiquetaId));
    }

    @Test
    void crearEtiqueta() throws Exception {
        String nombre = "Etiqueta de prueba " + System.nanoTime();
        mvc.perform(post("/api/etiquetas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value(nombre));
        entityManager.flush();
        entityManager.clear();
        org.junit.jupiter.api.Assertions.assertEquals(1L,
                entityManager.createQuery("select count(e) from Etiqueta e where e.nombre = :nombre", Long.class)
                        .setParameter("nombre", nombre).getSingleResult());
    }

    @Test
    void listarSoloProductosDeLaCategoriaSolicitada() throws Exception {
        Categoria seleccionada = new Categoria();
        seleccionada.setNombre("Categoría seleccionada");
        seleccionada = categorias.saveAndFlush(seleccionada);
        Categoria otra = new Categoria();
        otra.setNombre("Otra categoría");
        otra = categorias.saveAndFlush(otra);

        for (int indice = 0; indice < 3; indice++) {
            Producto producto = new Producto();
            producto.setCodigo("CAT-" + System.nanoTime());
            producto.setNombre("Producto " + indice);
            producto.setPrecioVenta(new java.math.BigDecimal("10.00"));
            producto.setCategoria(indice < 2 ? seleccionada : otra);
            productos.saveAndFlush(producto);
        }
        entityManager.clear();

        mvc.perform(get("/api/productos/categoria/{categoriaId}", seleccionada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[*].categoria.id",
                        org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(seleccionada.getId()))));
    }

    @Test
    void listarCategoriaSinProductosDevuelveListaVacia() throws Exception {
        Categoria vacia = new Categoria();
        vacia.setNombre("Categoría vacía");
        vacia = categorias.saveAndFlush(vacia);

        mvc.perform(get("/api/productos/categoria/{categoriaId}", vacia.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        mvc.perform(get("/api/productos/categoria/-1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void eliminarProductoDevuelve204SinContenido() throws Exception {
        Categoria categoria = new Categoria();
        categoria.setNombre("Categoría para eliminación");
        categoria = categorias.saveAndFlush(categoria);
        Producto producto = new Producto();
        producto.setCodigo("DEL-" + System.nanoTime());
        producto.setNombre("Producto temporal");
        producto.setPrecioVenta(new java.math.BigDecimal("10.00"));
        producto.setCategoria(categoria);
        Integer id = productos.saveAndFlush(producto).getId();

        mvc.perform(delete("/api/productos/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        entityManager.flush();
        entityManager.clear();
        mvc.perform(get("/api/productos/{id}", id))
                .andExpect(status().isNotFound());
        org.junit.jupiter.api.Assertions.assertTrue(categorias.existsById(categoria.getId()));
    }

    @Test
    void eliminarProductoInexistente() throws Exception {
        mvc.perform(delete("/api/productos/-1"))
                .andExpect(status().isNotFound());
    }

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
