package co.edu.pediatria.vacunacion.infraestructura;

import co.edu.pediatria.vacunacion.aplicacion.HistorialDosis;
import co.edu.pediatria.vacunacion.aplicacion.PublicadorEventos;
import co.edu.pediatria.vacunacion.aplicacion.RegistrarDosisServicio;
import co.edu.pediatria.vacunacion.dominio.EsquemaVacunacion;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "pediatria.eventos";
    public static final String ROUTING_KEY = "vacunacion.dosis-aplicada";

    @Bean
    public TopicExchange exchangeEventos() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter conversorJson(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RegistrarDosisServicio registrarDosisServicio(HistorialDosis historial, PublicadorEventos publicador) {
        return new RegistrarDosisServicio(new EsquemaVacunacion(), historial, publicador);
    }
}
