package ni.edu.uam.gestionproductos.service;

import java.util.List;
import ni.edu.uam.gestionproductos.dto.ProductoRequestDTO;
import ni.edu.uam.gestionproductos.entity.Categoria;
import ni.edu.uam.gestionproductos.entity.Producto;
import ni.edu.uam.gestionproductos.repository.CategoriaRepository;
import ni.edu.uam.gestionproductos.repository.ProductoRepository;
import ni.edu.uam.gestionproductos.repository.ProveedorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedores;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository,
                           ProveedorRepository proveedores) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.proveedores = proveedores;
    }

    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    public Producto buscarPorId(Integer id) {
        return productoRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    @Transactional
    public Producto guardar(Producto producto) {
        Integer categoriaId = producto.getCategoria() == null ? null : producto.getCategoria().getId();
        if (categoriaId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar categoria.id");
        }
        producto.setCategoria(categoriaRepository.findById(categoriaId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada")));
        if (producto.getProveedor() != null) {
            Integer proveedorId = producto.getProveedor().getId();
            if (proveedorId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar proveedor.id");
            }
            producto.setProveedor(proveedores.findById(proveedorId).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado")));
        }
        return productoRepository.saveAndFlush(producto);
    }

    @Transactional
    public Producto guardar(ProductoRequestDTO dto) {

        Categoria categoria = categoriaRepository
                .findById(dto.getCategoriaId())
                .orElseThrow(() ->
                        new RuntimeException("Categoría no encontrada"));

        Producto producto = new Producto();

        producto.setCodigo(dto.getCodigo());
        producto.setNombre(dto.getNombre());
        producto.setPrecioVenta(dto.getPrecioVenta());
        producto.setExistencia(dto.getExistencia());
        producto.setCategoria(categoria);

        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(
            Integer id,
            ProductoRequestDTO dto) {

        Producto producto = buscarPorId(id);

        Categoria categoria = categoriaRepository
                .findById(dto.getCategoriaId())
                .orElseThrow(() ->
                        new RuntimeException("Categoría no encontrada"));

        producto.setCodigo(dto.getCodigo());
        producto.setNombre(dto.getNombre());
        producto.setPrecioVenta(dto.getPrecioVenta());
        producto.setExistencia(dto.getExistencia());
        producto.setCategoria(categoria);

        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminar(Integer id) {
        productoRepository.delete(buscarPorId(id));
    }
}
