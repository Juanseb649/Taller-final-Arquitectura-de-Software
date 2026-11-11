package co.edu.pediatria.notificaciones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class NotificacionesListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionesListener.class);

    private final List<Notificacion> enviadas = new CopyOnWriteArrayList<>();

    @RabbitListener(queues = RabbitConfig.COLA)
    public void alAplicarDosis(DosisAplicada evento) {
        String mensaje = "Al paciente " + evento.pacienteId() + " se le aplicó la dosis "
                + evento.numeroDosis() + " de " + evento.vacuna() + " el " + evento.fechaAplicacion();
        enviadas.add(new Notificacion(evento.eventoId(), evento.pacienteId(), mensaje, Instant.now()));
        log.info("[NOTIFICACIÓN AL ACUDIENTE] {}", mensaje);
    }

    public List<Notificacion> enviadas() {
        return List.copyOf(enviadas);
    }
}
