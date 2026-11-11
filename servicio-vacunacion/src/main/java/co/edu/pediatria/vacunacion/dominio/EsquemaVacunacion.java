package co.edu.pediatria.vacunacion.dominio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

public class EsquemaVacunacion {

    private record Regla(int dosisTotales, int intervaloMinimoDias) {
    }

    private static final Map<String, Regla> REGLAS = Map.of(
            "BCG", new Regla(1, 0),
            "HEPATITIS_B", new Regla(1, 0),
            "PENTAVALENTE", new Regla(3, 28),
            "POLIO", new Regla(3, 28),
            "NEUMOCOCO", new Regla(3, 56),
            "TRIPLE_VIRAL", new Regla(2, 180));

    public void validar(AplicacionDosis aplicacion, Optional<AplicacionDosis> anterior, LocalDate hoy) {
        Regla regla = Optional.ofNullable(REGLAS.get(aplicacion.vacuna()))
                .orElseThrow(() -> new ReglaVacunacionException(
                        "La vacuna " + aplicacion.vacuna() + " no está en el esquema"));

        if (aplicacion.fechaAplicacion().isAfter(hoy)) {
            throw new ReglaVacunacionException("La fecha de aplicación no puede ser futura");
        }

        if (aplicacion.numeroDosis() < 1 || aplicacion.numeroDosis() > regla.dosisTotales()) {
            throw new ReglaVacunacionException(
                    aplicacion.vacuna() + " tiene " + regla.dosisTotales() + " dosis en el esquema");
        }

        int dosisEsperada = anterior.map(a -> a.numeroDosis() + 1).orElse(1);
        if (aplicacion.numeroDosis() != dosisEsperada) {
            throw new ReglaVacunacionException(
                    "Se esperaba la dosis " + dosisEsperada + " de " + aplicacion.vacuna());
        }

        anterior.ifPresent(previa -> {
            long dias = ChronoUnit.DAYS.between(previa.fechaAplicacion(), aplicacion.fechaAplicacion());
            if (dias < regla.intervaloMinimoDias()) {
                throw new ReglaVacunacionException(
                        "Deben pasar al menos " + regla.intervaloMinimoDias()
                                + " días desde la dosis anterior; han pasado " + dias);
            }
        });
    }
}
