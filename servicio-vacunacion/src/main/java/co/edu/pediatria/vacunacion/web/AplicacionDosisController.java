package co.edu.pediatria.vacunacion.web;

import co.edu.pediatria.vacunacion.aplicacion.RegistrarDosisServicio;
import co.edu.pediatria.vacunacion.dominio.AplicacionDosis;
import co.edu.pediatria.vacunacion.dominio.DosisAplicada;
import co.edu.pediatria.vacunacion.dominio.ReglaVacunacionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/vacunas")
public class AplicacionDosisController {

    public record SolicitudAplicacion(String pacienteId, String vacuna, int numeroDosis, LocalDate fechaAplicacion) {
    }

    private final RegistrarDosisServicio servicio;

    public AplicacionDosisController(RegistrarDosisServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/aplicaciones")
    public ResponseEntity<DosisAplicada> registrar(@RequestBody SolicitudAplicacion solicitud) {
        LocalDate fecha = solicitud.fechaAplicacion() != null ? solicitud.fechaAplicacion() : LocalDate.now();
        AplicacionDosis aplicacion = new AplicacionDosis(
                solicitud.pacienteId(), solicitud.vacuna(), solicitud.numeroDosis(), fecha);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrar(aplicacion));
    }

    @ExceptionHandler(ReglaVacunacionException.class)
    public ResponseEntity<Map<String, String>> reglaIncumplida(ReglaVacunacionException e) {
        return ResponseEntity.unprocessableEntity().body(Map.of("error", e.getMessage()));
    }
}
