package co.edu.pediatria.vacunacion.infraestructura;

import co.edu.pediatria.vacunacion.aplicacion.HistorialDosis;
import co.edu.pediatria.vacunacion.dominio.AplicacionDosis;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HistorialDosisEnMemoria implements HistorialDosis {

    private final Map<String, AplicacionDosis> ultimas = new ConcurrentHashMap<>();

    @Override
    public Optional<AplicacionDosis> ultimaDosis(String pacienteId, String vacuna) {
        return Optional.ofNullable(ultimas.get(clave(pacienteId, vacuna)));
    }

    @Override
    public void guardar(AplicacionDosis aplicacion) {
        ultimas.put(clave(aplicacion.pacienteId(), aplicacion.vacuna()), aplicacion);
    }

    private String clave(String pacienteId, String vacuna) {
        return pacienteId + "|" + vacuna;
    }
}
