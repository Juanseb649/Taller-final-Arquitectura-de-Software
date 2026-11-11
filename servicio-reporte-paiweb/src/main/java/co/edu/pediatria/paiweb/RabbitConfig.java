package co.edu.pediatria.paiweb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
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
    public static final String COLA = "paiweb.dosis-aplicada";
    public static final String EXCHANGE_ERRORES = "pediatria.errores";
    public static final String COLA_ERRORES = "paiweb.dosis-aplicada.dlq";

    @Bean
    public TopicExchange exchangeEventos() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange exchangeErrores() {
        return new DirectExchange(EXCHANGE_ERRORES, true, false);
    }

    @Bean
    public Queue colaPaiweb() {
        return QueueBuilder.durable(COLA)
                .deadLetterExchange(EXCHANGE_ERRORES)
                .deadLetterRoutingKey(COLA_ERRORES)
                .build();
    }

    @Bean
    public Queue colaErroresPaiweb() {
        return QueueBuilder.durable(COLA_ERRORES).build();
    }

    @Bean
    public Binding enlacePaiweb(Queue colaPaiweb, TopicExchange exchangeEventos) {
        return BindingBuilder.bind(colaPaiweb).to(exchangeEventos).with(ROUTING_KEY);
    }

    @Bean
    public Binding enlaceErroresPaiweb(Queue colaErroresPaiweb, DirectExchange exchangeErrores) {
        return BindingBuilder.bind(colaErroresPaiweb).to(exchangeErrores).with(COLA_ERRORES);
    }

    @Bean
    public MessageConverter conversorJson(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter conversor = new Jackson2JsonMessageConverter(objectMapper);
        conversor.setAlwaysConvertToInferredType(true);
        return conversor;
    }
}
