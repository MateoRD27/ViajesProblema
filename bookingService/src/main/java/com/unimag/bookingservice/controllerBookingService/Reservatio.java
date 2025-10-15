package com.unimag.bookingservice.controllerBookingService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservatio {
    private Integer id;
    private String cliente;
    private String tipo;
    private String estado;
    private String itineraryId;
    private String paymentToken;
}
