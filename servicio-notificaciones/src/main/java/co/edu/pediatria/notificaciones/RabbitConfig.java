package co.edu.pediatria.notificaciones;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "pediatria.eventos";
    public static final String ROUTING_KEY = "vacunacion.dosis-aplicada";
    public static final String COLA = "notificaciones.dosis-aplicada";

    @Bean
    public TopicExchange exchangeEventos() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue colaNotificaciones() {
        return QueueBuilder.durable(COLA).build();
    }

    @Bean
    public Binding enlaceNotificaciones(Queue colaNotificaciones, TopicExchange exchangeEventos) {
        return BindingBuilder.bind(colaNotificaciones).to(exchangeEventos).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter conversorJson(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter conversor = new Jackson2JsonMessageConverter(objectMapper);
        conversor.setAlwaysConvertToInferredType(true);
        return conversor;
    }
}
