package ni.edu.uam.gestionproductos.controller;

import java.util.List;
import ni.edu.uam.gestionproductos.dto.ProductoRequestDTO;
import ni.edu.uam.gestionproductos.entity.Producto;
import ni.edu.uam.gestionproductos.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> listar() {
        return productoService.listar();
    }

    @GetMapping("/categoria/{categoriaId}")
    public List<Producto> listarPorCategoria(
            @PathVariable Integer categoriaId) {

        return productoService
                .listarPorCategoria(categoriaId);
    }

    @GetMapping("/{id}")
    public Producto buscar(@PathVariable Integer id) {
        return productoService.buscarPorId(id);
    }

    @PostMapping
    public Producto guardar(
            @RequestBody ProductoRequestDTO dto) {

        return productoService.guardar(dto);
    }

    @PutMapping("/{id}")
    public Producto actualizar(
            @PathVariable Integer id,
            @RequestBody ProductoRequestDTO dto) {

        return productoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer id) {

        productoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    // --- ENDPOINT AGREGADO AQUÍ (Paso 14) ---
    @PostMapping("/{productoId}/etiquetas/{etiquetaId}")
    public ResponseEntity<Producto> asociarEtiqueta(
            @PathVariable Integer productoId,
            @PathVariable Integer etiquetaId) {

        Producto productoActualizado = productoService.agregarEtiqueta(productoId, etiquetaId);
        return new ResponseEntity<>(productoActualizado, HttpStatus.OK);
    }

    // --- ENDPOINT PARA EL RETO 1 (Eliminar asociación) ---
    @DeleteMapping("/{productoId}/etiquetas/{etiquetaId}")
    public ResponseEntity<Producto> eliminarAsociacionEtiqueta(
            @PathVariable Integer productoId,
            @PathVariable Integer etiquetaId) {

        Producto productoActualizado = productoService.removerEtiqueta(productoId, etiquetaId);
        return new ResponseEntity<>(productoActualizado, HttpStatus.OK);
    }

    // --- ENDPOINT PARA EL RETO 2 (Consultar productos por etiqueta) ---
    @GetMapping("/etiqueta/{etiquetaId}")
    public ResponseEntity<List<Producto>> consultarPorEtiqueta(
            @PathVariable Integer etiquetaId) {

        List<Producto> productos = productoService.listarPorEtiqueta(etiquetaId);
        return new ResponseEntity<>(productos, HttpStatus.OK);
    }
}