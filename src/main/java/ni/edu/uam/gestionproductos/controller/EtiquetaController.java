package ni.edu.uam.gestionproductos.controller;

import ni.edu.uam.gestionproductos.entity.Etiqueta;
import jakarta.validation.Valid;
import ni.edu.uam.gestionproductos.dto.EtiquetaRequestDTO;
import ni.edu.uam.gestionproductos.service.EtiquetaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/etiquetas")
public class EtiquetaController {

    private final EtiquetaService etiquetaService;

    public EtiquetaController(EtiquetaService etiquetaService) {
        this.etiquetaService = etiquetaService;
    }

    @PostMapping
    public ResponseEntity<Etiqueta> crearEtiqueta(@Valid @RequestBody EtiquetaRequestDTO dto) {
        Etiqueta guardada = etiquetaService.guardar(dto);
        return new ResponseEntity<>(guardada, HttpStatus.CREATED);
    }
}
