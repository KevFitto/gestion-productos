package ni.edu.uam.gestionproductos.service;

import ni.edu.uam.gestionproductos.dto.EtiquetaRequestDTO;
import ni.edu.uam.gestionproductos.entity.Etiqueta;
import ni.edu.uam.gestionproductos.repository.EtiquetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EtiquetaService {
    private final EtiquetaRepository repository;

    public EtiquetaService(EtiquetaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Etiqueta guardar(EtiquetaRequestDTO dto) {
        Etiqueta entidad = new Etiqueta();
        entidad.setNombre(dto.getNombre());
        return repository.save(entidad);
    }
}
