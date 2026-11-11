package co.edu.pediatria.vacunacion.aplicacion;

import co.edu.pediatria.vacunacion.dominio.DosisAplicada;

public interface PublicadorEventos {

    void publicar(DosisAplicada evento);
}
