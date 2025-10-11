package com.unimag.bookingservice.controllerBookingService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/booking/v1")
public class controllerBookingService {

    // Lista simulada de reservas en memoria
    private static final List<Reservatio> reservas = new ArrayList<>();
    private static int currentId = 1;

    // Bloque estático: se ejecuta una sola vez al iniciar la app
    static {
        // Crear 10 reservas simuladas
        for (int i = 1; i <= 10; i++) {
            Reservatio reserva = Reservatio.builder()
                    .id(i)
                    .cliente("Cliente " + i)
                    .tipo(i % 2 == 0 ? "vuelo" : "hotel")
                    .estado("CONFIRMADA")
                    .build();
            reservas.add(reserva);
        }
        currentId = 11; // siguiente ID disponible
    }

    // POST /api/booking/v1/reservations
    @PostMapping("/reservations")
    public ResponseEntity<String> createReservation(@RequestBody Reservatio nuevaReserva) {
        nuevaReserva.setId(currentId++);
        nuevaReserva.setEstado("CONFIRMADA");
        reservas.add(nuevaReserva);
        return ResponseEntity.ok("Reserva creada con éxito. ID: " + nuevaReserva.getId());
    }

    // GET /api/booking/v1/reservations/{id}
    @GetMapping("/reservations/{id}")
    public ResponseEntity<?> getReservationStatus(@PathVariable int id) {
        for (Reservatio r : reservas) {
            if (r.getId() == id) {
                return ResponseEntity.ok(r);
            }
        }
        return ResponseEntity.status(404).body("Reserva no encontrada con ID: " + id);
    }

    // GET /api/booking/v1/reservations
    @GetMapping("/reservations")
    public ResponseEntity<List<Reservatio>> getAllReservations() {
        return ResponseEntity.ok(reservas);
    }

}
