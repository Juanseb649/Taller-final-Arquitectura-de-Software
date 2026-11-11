package co.edu.pediatria.vacunacion.aplicacion;

import co.edu.pediatria.vacunacion.dominio.AplicacionDosis;

import java.util.Optional;

public interface HistorialDosis {

    Optional<AplicacionDosis> ultimaDosis(String pacienteId, String vacuna);

    void guardar(AplicacionDosis aplicacion);
}
