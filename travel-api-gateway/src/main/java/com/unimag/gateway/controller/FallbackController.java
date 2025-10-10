package com.unimag.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/itinerary")
    public ResponseEntity<?> itineraryFallback() {
        return ResponseEntity.status(503)
                .body(Map.of("message", "Itinerary Service no disponible. Intente más tarde."));
    }

    @RequestMapping("/booking")
    public ResponseEntity<?> bookingFallback() {
        return ResponseEntity.status(503)
                .body(Map.of("message", "Booking Service no disponible. Intente más tarde."));
    }
}

