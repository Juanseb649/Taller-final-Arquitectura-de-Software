package co.edu.pediatria.paiweb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ClientePaiwebSimulado {

    private static final Logger log = LoggerFactory.getLogger(ClientePaiwebSimulado.class);

    private final AtomicBoolean disponible;

    public ClientePaiwebSimulado(@Value("${paiweb.disponible:true}") boolean disponible) {
        this.disponible = new AtomicBoolean(disponible);
    }

    public void reportar(DosisAplicada evento) {
        if (!disponible.get()) {
            log.warn("PAIWEB no disponible; falla el reporte del evento {}", evento.eventoId());
            throw new IllegalStateException("PAIWEB no responde");
        }
        log.info("[PAIWEB] Reportada dosis {} de {} del paciente {}",
                evento.numeroDosis(), evento.vacuna(), evento.pacienteId());
    }

    public boolean disponible() {
        return disponible.get();
    }

    public void cambiarDisponibilidad(boolean valor) {
        disponible.set(valor);
    }
}
