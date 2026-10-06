package ni.edu.uam.gestionproductos.repository;

import ni.edu.uam.gestionproductos.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}

