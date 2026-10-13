package ni.edu.uam.gestionproductos.service;

import java.util.List;
import ni.edu.uam.gestionproductos.dto.CategoriaRequestDTO;
import ni.edu.uam.gestionproductos.entity.Categoria;
import ni.edu.uam.gestionproductos.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

    public Categoria buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }

    @Transactional
    public Categoria guardar(CategoriaRequestDTO dto) {
        Categoria entidad = new Categoria();
        entidad.setNombre(dto.getNombre());
        entidad.setActiva(dto.isActiva());
        return repository.save(entidad);
    }

    @Transactional
    public Categoria actualizar(Integer id, CategoriaRequestDTO dto) {
        Categoria entidad = buscarPorId(id);
        entidad.setNombre(dto.getNombre());
        entidad.setActiva(dto.isActiva());
        return repository.save(entidad);
    }

    @Transactional
    public void eliminar(Integer id) {
        repository.delete(buscarPorId(id));
        repository.flush();
    }
}
