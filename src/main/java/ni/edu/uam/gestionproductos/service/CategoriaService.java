package ni.edu.uam.gestionproductos.service;

import java.util.List;
import ni.edu.uam.gestionproductos.dto.CategoriaRequestDTO;
import ni.edu.uam.gestionproductos.entity.Categoria;
import ni.edu.uam.gestionproductos.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoriaService {
    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public List<Categoria> listar() {
        return repository.findAll();
    }

    @Transactional
    public Categoria guardar(CategoriaRequestDTO dto) {
        Categoria entidad = new Categoria();
        entidad.setNombre(dto.getNombre());
        entidad.setActiva(dto.isActiva());
        return repository.save(entidad);
    }
}
