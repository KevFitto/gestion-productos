package ni.edu.uam.gestionproductos.controller;

import jakarta.validation.Valid;
import java.util.List;
import ni.edu.uam.gestionproductos.entity.Proveedor;
import ni.edu.uam.gestionproductos.repository.ProveedorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {
    private final ProveedorRepository repository;

    public ProveedorController(ProveedorRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Proveedor> listar() { return repository.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Proveedor guardar(@Valid @RequestBody Proveedor entidad) {
        entidad.setId(null);
        return repository.save(entidad);
    }
}

