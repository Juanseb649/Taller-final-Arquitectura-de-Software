package co.edu.pediatria.notificaciones;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NotificacionesController {

    private final NotificacionesListener listener;

    public NotificacionesController(NotificacionesListener listener) {
        this.listener = listener;
    }

    @GetMapping("/api/notificaciones")
    public List<Notificacion> listar() {
        return listener.enviadas();
    }
}
