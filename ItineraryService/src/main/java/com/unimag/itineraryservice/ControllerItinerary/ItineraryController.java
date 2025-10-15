package com.unimag.itineraryservice.ControllerItinerary;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import lombok.*;

// Modelo simple de itinerario
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Itinerary {
    private int id;
    private String origin;
    private String destination;
    private String departDate;
    private String returnDate;
    private int adults;
    private int rooms;
    private double price;
}

// Controlador
@RestController
@RequestMapping("/api/itinerary/v1")
public class ItineraryController {

    private static final List<Itinerary> itineraries = new ArrayList<>();
    private static int currentId = 1;

    // Bloque estático: crea itinerarios simulados
    static {
        for (int i = 1; i <= 10; i++) {
            Itinerary iti = Itinerary.builder()
                    .id(i)
                    .origin("Ciudad " + i)
                    .destination("Destino " + (i + 1))
                    .departDate("2025-10-" + (10 + i))
                    .returnDate("2025-10-" + (15 + i))
                    .adults((i % 3) + 1)
                    .rooms((i % 2) + 1)
                    .price(200 + i * 50)
                    .build();
            itineraries.add(iti);
        }
        currentId = 11;
    }

    // GET /api/itinerary/v1/search
    @GetMapping("/search")
    public ResponseEntity<List<Itinerary>> searchItineraries(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String departDate,
            @RequestParam(required = false) String returnDate,
            @RequestParam(required = false) Integer adults,
            @RequestParam(required = false) Integer rooms
    ) {
        List<Itinerary> results = new ArrayList<>();

        for (Itinerary i : itineraries) {
            boolean match = true;
            if (origin != null && !i.getOrigin().equalsIgnoreCase(origin)) match = false;
            if (destination != null && !i.getDestination().equalsIgnoreCase(destination)) match = false;
            if (adults != null && i.getAdults() != adults) match = false;
            if (rooms != null && i.getRooms() != rooms) match = false;
            if (match) results.add(i);
        }

        return ResponseEntity.ok(results);
    }

    // GET /api/itinerary/v1/details/{itineraryId}
    @GetMapping("/details/{itineraryId}")
    public ResponseEntity<?> getItineraryDetails(@PathVariable int itineraryId) {
        for (Itinerary i : itineraries) {
            if (i.getId() == itineraryId) {
                return ResponseEntity.ok(i);
            }
        }
        return ResponseEntity.status(404).body("Itinerario no encontrado con ID: " + itineraryId);
    }

    // POST /api/itinerary/v1/user-itineraries
    @PostMapping("/user-itineraries")
    public ResponseEntity<String> addUserItinerary(@RequestBody Itinerary nuevo) {
        nuevo.setId(currentId++);
        itineraries.add(nuevo);
        return ResponseEntity.ok("Itinerario guardado para el usuario. ID: " + nuevo.getId());
    }

    // GET /api/itinerary/v1/user-itineraries/{id}
    @GetMapping("/user-itineraries/{id}")
    public ResponseEntity<?> getUserItinerary(@PathVariable int id) {
        for (Itinerary i : itineraries) {
            if (i.getId() == id) {
                return ResponseEntity.ok(i);
            }
        }
        return ResponseEntity.status(404).body("Itinerario de usuario no encontrado con ID: " + id);
    }

    // DELETE /api/itinerary/v1/user-itineraries/{id}
    @DeleteMapping("/user-itineraries/{id}")
    public ResponseEntity<String> deleteUserItinerary(@PathVariable int id) {
        Iterator<Itinerary> iterator = itineraries.iterator();
        while (iterator.hasNext()) {
            Itinerary i = iterator.next();
            if (i.getId() == id) {
                iterator.remove();
                return ResponseEntity.ok("Itinerario de usuario eliminado con ID: " + id);
            }
        }
        return ResponseEntity.status(404).body("No se encontró el itinerario con ID: " + id);
    }
}
