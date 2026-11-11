package co.edu.pediatria.paiweb;

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
}
