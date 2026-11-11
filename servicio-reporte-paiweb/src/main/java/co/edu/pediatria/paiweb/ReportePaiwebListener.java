package co.edu.pediatria.paiweb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ReportePaiwebListener {

    private static final Logger log = LoggerFactory.getLogger(ReportePaiwebListener.class);

    private final ClientePaiwebSimulado cliente;
    private final Set<UUID> procesados = ConcurrentHashMap.newKeySet();
    private final List<DosisAplicada> reportados = new CopyOnWriteArrayList<>();

    public ReportePaiwebListener(ClientePaiwebSimulado cliente) {
        this.cliente = cliente;
    }

    @RabbitListener(queues = RabbitConfig.COLA)
    public void alAplicarDosis(DosisAplicada evento) {
        if (procesados.contains(evento.eventoId())) {
            log.info("Evento {} ya reportado; se ignora el duplicado", evento.eventoId());
            return;
        }
        cliente.reportar(evento);
        procesados.add(evento.eventoId());
        reportados.add(evento);
    }

    public List<DosisAplicada> reportados() {
        return List.copyOf(reportados);
    }
}
