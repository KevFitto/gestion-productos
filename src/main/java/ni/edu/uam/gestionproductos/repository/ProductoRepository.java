package ni.edu.uam.gestionproductos.repository;

import ni.edu.uam.gestionproductos.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
}

