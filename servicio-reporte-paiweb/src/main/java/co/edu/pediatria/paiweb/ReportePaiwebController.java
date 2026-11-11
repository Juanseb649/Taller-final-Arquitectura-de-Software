package co.edu.pediatria.paiweb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paiweb")
public class ReportePaiwebController {

    private final ReportePaiwebListener listener;
    private final ClientePaiwebSimulado cliente;

    public ReportePaiwebController(ReportePaiwebListener listener, ClientePaiwebSimulado cliente) {
        this.listener = listener;
        this.cliente = cliente;
    }

    @GetMapping("/reportes")
    public List<DosisAplicada> reportes() {
        return listener.reportados();
    }

    @PostMapping("/disponibilidad")
    public Map<String, Boolean> cambiarDisponibilidad(@RequestParam boolean valor) {
        cliente.cambiarDisponibilidad(valor);
        return Map.of("disponible", cliente.disponible());
    }
}
