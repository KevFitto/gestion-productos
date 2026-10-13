package ni.edu.uam.gestionproductos.service;

import java.util.List;
import ni.edu.uam.gestionproductos.dto.ProveedorRequestDTO;
import ni.edu.uam.gestionproductos.entity.Proveedor;
import ni.edu.uam.gestionproductos.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProveedorService {
    private final ProveedorRepository repository;

    public ProveedorService(ProveedorRepository repository) {
        this.repository = repository;
    }

    public List<Proveedor> listar() {
        return repository.findAll();
    }

    @Transactional
    public Proveedor guardar(ProveedorRequestDTO dto) {
        Proveedor entidad = new Proveedor();
        entidad.setNombre(dto.getNombre());
        entidad.setTelefono(dto.getTelefono());
        entidad.setCorreo(dto.getCorreo());
        entidad.setActivo(dto.isActivo());
        return repository.save(entidad);
    }
}
