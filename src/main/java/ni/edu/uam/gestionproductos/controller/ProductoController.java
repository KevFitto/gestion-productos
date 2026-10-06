package ni.edu.uam.gestionproductos.controller;

import jakarta.validation.Valid;
import java.util.List;
import ni.edu.uam.gestionproductos.entity.Producto;
import ni.edu.uam.gestionproductos.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoRepository repository;
    private final CategoriaRepository categorias;
    private final ProveedorRepository proveedores;

    public ProductoController(ProductoRepository repository, CategoriaRepository categorias,
                              ProveedorRepository proveedores) {
        this.repository = repository;
        this.categorias = categorias;
        this.proveedores = proveedores;
    }

    @GetMapping
    public List<Producto> listar() { return repository.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto guardar(@Valid @RequestBody Producto producto) {
        Integer categoriaId = producto.getCategoria().getId();
        if (categoriaId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar categoria.id");
        }
        producto.setCategoria(categorias.findById(categoriaId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada")));
        if (producto.getProveedor() != null) {
            Integer proveedorId = producto.getProveedor().getId();
            if (proveedorId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar proveedor.id");
            }
            producto.setProveedor(proveedores.findById(proveedorId).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado")));
        }
        producto.setId(null);
        return repository.saveAndFlush(producto);
    }
}

