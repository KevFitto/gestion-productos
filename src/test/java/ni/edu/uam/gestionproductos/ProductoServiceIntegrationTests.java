package ni.edu.uam.gestionproductos;

import java.math.BigDecimal;
import ni.edu.uam.gestionproductos.entity.Categoria;
import ni.edu.uam.gestionproductos.entity.Producto;
import ni.edu.uam.gestionproductos.repository.CategoriaRepository;
import ni.edu.uam.gestionproductos.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductoServiceIntegrationTests {
    @Autowired ProductoService service;
    @Autowired CategoriaRepository categorias;

    @Test
    void guardarBuscarActualizarYEliminar() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Prueba de servicio");
        categoria = categorias.saveAndFlush(categoria);
        Producto producto = new Producto();
        producto.setCodigo("SVC-" + System.nanoTime());
        producto.setNombre("Producto de prueba");
        producto.setCategoria(categoria);
        producto.setPrecioVenta(new BigDecimal("10.00"));
        Integer id = service.guardar(producto).getId();
        assertEquals("Producto de prueba", service.buscarPorId(id).getNombre());
        producto.setNombre("Producto actualizado");
        assertEquals(id, service.guardar(producto).getId());
        assertEquals("Producto actualizado", service.buscarPorId(id).getNombre());
        service.eliminar(id);
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.buscarPorId(id));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void eliminarInexistenteDevuelveNoEncontrado() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.eliminar(-1));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }
}
