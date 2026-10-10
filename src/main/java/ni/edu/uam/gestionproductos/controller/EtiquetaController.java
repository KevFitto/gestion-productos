package ni.edu.uam.gestionproductos.controller;

import ni.edu.uam.gestionproductos.entity.Etiqueta;
import ni.edu.uam.gestionproductos.repository.EtiquetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/etiquetas")
public class EtiquetaController {

    @Autowired
    private EtiquetaRepository etiquetaRepository;

    @PostMapping
    public ResponseEntity<Etiqueta> crearEtiqueta(@RequestBody Etiqueta etiqueta) {
        Etiqueta guardada = etiquetaRepository.save(etiqueta);
        return new ResponseEntity<>(guardada, HttpStatus.CREATED);
    }
}