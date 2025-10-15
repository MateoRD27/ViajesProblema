package com.unimag.bookingservice.controllerBookingService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/booking/v1")
public class controllerBookingService {

    private static final List<Reservatio> reservas = new ArrayList<>();
    private static int currentId = 1;

    static {
        // Crear 10 reservas simuladas
        for (int i = 1; i <= 10; i++) {
            Reservatio reserva = Reservatio.builder()
                    .id(i)
                    .cliente("Cliente " + i)
                    .tipo(i % 2 == 0 ? "vuelo" : "hotel")
                    .estado("CONFIRMADA")
                    .itineraryId("ITI-" + i)
                    .paymentToken("tok_demo_" + i)
                    .build();
            reservas.add(reserva);
        }
        currentId = 11;
    }

    // DTO mínimo para recibir solo lo que necesitamos
    public static class CreateReservationRequest {
        public String itineraryId;
        public String paymentToken;
    }

    @PostMapping("/reservations")
    public ResponseEntity<String> createReservation(@RequestBody CreateReservationRequest request) {
        //  hardcode para cliente/tipo/estado
        Reservatio nuevaReserva = Reservatio.builder()
                .id(currentId)
                .cliente("Cliente " + currentId)
                .tipo(currentId % 2 == 0 ? "vuelo" : "hotel")
                .estado("CONFIRMADA")
                .itineraryId(request.itineraryId)
                .paymentToken(request.paymentToken)
                .build();

        reservas.add(nuevaReserva);
        currentId++;

        return ResponseEntity.ok("Reserva creada con éxito. ID: " + nuevaReserva.getId());
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<?> getReservationStatus(@PathVariable("id") int id) {
        for (Reservatio r : reservas) {
            if (r.getId() == id) {
                return ResponseEntity.ok(r);
            }
        }
        return ResponseEntity.status(404).body("Reserva no encontrada con ID: " + id);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<Reservatio>> getAllReservations() {
        return ResponseEntity.ok(reservas);
    }
}
