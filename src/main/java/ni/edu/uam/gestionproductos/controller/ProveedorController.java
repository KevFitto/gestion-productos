package ni.edu.uam.gestionproductos.controller;

import jakarta.validation.Valid;
import java.util.List;
import ni.edu.uam.gestionproductos.entity.Proveedor;
import ni.edu.uam.gestionproductos.dto.ProveedorRequestDTO;
import ni.edu.uam.gestionproductos.service.ProveedorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {
    private final ProveedorService service;

    public ProveedorController(ProveedorService service) {
        this.service = service;
    }

    @GetMapping
    public List<Proveedor> listar() { return service.listar(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Proveedor guardar(@Valid @RequestBody ProveedorRequestDTO dto) {
        return service.guardar(dto);
    }
}

