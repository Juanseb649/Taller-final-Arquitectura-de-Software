package co.edu.pediatria.vacunacion.dominio;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DosisAplicada(
        UUID eventoId,
        String pacienteId,
        String vacuna,
        int numeroDosis,
        LocalDate fechaAplicacion,
        Instant ocurridoEn) {

    public static DosisAplicada desde(AplicacionDosis aplicacion) {
        return new DosisAplicada(
                UUID.randomUUID(),
                aplicacion.pacienteId(),
                aplicacion.vacuna(),
                aplicacion.numeroDosis(),
                aplicacion.fechaAplicacion(),
                Instant.now());
    }
}
