package co.edu.pediatria.notificaciones;

import java.time.Instant;
import java.util.UUID;

public record Notificacion(UUID eventoId, String pacienteId, String mensaje, Instant enviadaEn) {
}
