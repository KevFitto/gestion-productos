package ni.edu.uam.gestionproductos.repository;

import java.util.List;
import ni.edu.uam.gestionproductos.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByCategoriaId(Integer categoriaId);

    // Método agregado para el Reto 2
    List<Producto> findByEtiquetasId(Integer etiquetaId);
}