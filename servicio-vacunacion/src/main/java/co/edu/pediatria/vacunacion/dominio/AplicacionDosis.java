package co.edu.pediatria.vacunacion.dominio;

import java.time.LocalDate;

public record AplicacionDosis(String pacienteId, String vacuna, int numeroDosis, LocalDate fechaAplicacion) {
}
