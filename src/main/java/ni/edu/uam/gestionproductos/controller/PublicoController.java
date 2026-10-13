package ni.edu.uam.gestionproductos.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/publico")
public class PublicoController {
    @GetMapping("/saludo")
    public Map<String, String> saludo() {
        return Map.of("mensaje", "API NicaMarket operativa");
    }
}
