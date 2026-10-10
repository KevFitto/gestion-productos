package ni.edu.uam.gestionproductos;

import java.math.BigDecimal;
import jakarta.persistence.EntityManager;
import ni.edu.uam.gestionproductos.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductosPorEtiquetaIntegrationTests {

    @Autowired MockMvc mvc;
    @Autowired EntityManager em;
    Integer[] productos = new Integer[3];
    Integer ofertaId;
    Integer otraId;

    @BeforeEach
    void preparar() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Retos");
        em.persist(categoria);
        Etiqueta oferta = new Etiqueta();
        oferta.setNombre("Oferta-" + System.nanoTime());
        em.persist(oferta);
        Etiqueta otra = new Etiqueta();
        otra.setNombre("Otra-" + System.nanoTime());
        em.persist(otra);
        for (int i = 0; i < 3; i++) {
            Producto producto = new Producto();
            producto.setCodigo("RETO-" + System.nanoTime());
            producto.setNombre("Producto " + i);
            producto.setPrecioVenta(new BigDecimal("10.00"));
            producto.setCategoria(categoria);
            if (i < 2) producto.getEtiquetas().add(oferta);
            if (i != 1) producto.getEtiquetas().add(otra);
            em.persist(producto);
            productos[i] = producto.getId();
        }
        ofertaId = oferta.getId();
        otraId = otra.getId();
        em.flush();
        em.clear();
    }

    @Test
    void filtrarProductosSinDuplicados() throws Exception {
        mvc.perform(get("/api/productos/etiqueta/{id}", ofertaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(productos[0], productos[1])));
        mvc.perform(delete("/api/productos/{p}/etiquetas/{e}", productos[0], ofertaId))
                .andExpect(status().isOk());
        em.flush();
        em.clear();
        mvc.perform(get("/api/productos/etiqueta/{id}", ofertaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", contains(productos[1])));
    }

    @Test
    void etiquetaSinProductosDevuelveListaVacia() throws Exception {
        Etiqueta vacia = new Etiqueta();
        vacia.setNombre("Sin productos-" + System.nanoTime());
        em.persist(vacia);
        em.flush();
        mvc.perform(get("/api/productos/etiqueta/{id}", vacia.getId()))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(get("/api/productos/etiqueta/-1"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }
}
