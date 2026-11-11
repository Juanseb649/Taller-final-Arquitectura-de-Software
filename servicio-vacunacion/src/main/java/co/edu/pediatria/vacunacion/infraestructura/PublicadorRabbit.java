package co.edu.pediatria.vacunacion.infraestructura;

import co.edu.pediatria.vacunacion.aplicacion.PublicadorEventos;
import co.edu.pediatria.vacunacion.dominio.DosisAplicada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PublicadorRabbit implements PublicadorEventos {

    private static final Logger log = LoggerFactory.getLogger(PublicadorRabbit.class);

    private final RabbitTemplate rabbitTemplate;

    public PublicadorRabbit(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicar(DosisAplicada evento) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, evento);
        log.info("Evento DosisAplicada publicado: {} dosis {} paciente {} (evento {})",
                evento.vacuna(), evento.numeroDosis(), evento.pacienteId(), evento.eventoId());
    }
}
