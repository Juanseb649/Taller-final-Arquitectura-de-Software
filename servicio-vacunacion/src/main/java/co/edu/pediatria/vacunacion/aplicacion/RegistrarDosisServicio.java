package co.edu.pediatria.vacunacion.aplicacion;

import co.edu.pediatria.vacunacion.dominio.AplicacionDosis;
import co.edu.pediatria.vacunacion.dominio.DosisAplicada;
import co.edu.pediatria.vacunacion.dominio.EsquemaVacunacion;

import java.time.LocalDate;

public class RegistrarDosisServicio {

    private final EsquemaVacunacion esquema;
    private final HistorialDosis historial;
    private final PublicadorEventos publicador;

    public RegistrarDosisServicio(EsquemaVacunacion esquema, HistorialDosis historial, PublicadorEventos publicador) {
        this.esquema = esquema;
        this.historial = historial;
        this.publicador = publicador;
    }

    public DosisAplicada registrar(AplicacionDosis aplicacion) {
        esquema.validar(aplicacion, historial.ultimaDosis(aplicacion.pacienteId(), aplicacion.vacuna()), LocalDate.now());
        historial.guardar(aplicacion);
        DosisAplicada evento = DosisAplicada.desde(aplicacion);
        publicador.publicar(evento);
        return evento;
    }
}
