package ni.edu.uam.gestionproductos.repository;

import ni.edu.uam.gestionproductos.entity.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtiquetaRepository
        extends JpaRepository<Etiqueta, Integer> {
}
