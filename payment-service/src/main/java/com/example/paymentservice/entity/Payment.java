package com.example.paymentservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data // Lombok para generar getters, setters, etc.
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Generación automática del ID
    private UUID id;

    @Column(nullable = false)
    private String transactionId; // Identificador único para la transacción

    @Column(nullable = false)
    private Double amount; // Monto de dinero del pago

    @Column(nullable = false)
    private String currency; // Moneda del pago, ejemplo: USD, EUR, etc.

    @Column(nullable = false)
    private String status; // Estado del pago (ejemplo: PENDING, SUCCESS, FAILED)

    @Column(nullable = false)
    private String paymentMethod; // Método de pago, ejemplo: tarjeta de crédito, PayPal, etc.

    @Column(nullable = false)
    private LocalDateTime paymentDate; // Fecha y hora del pago

    @Column(nullable = false)
    private String customerId; // ID del cliente asociado al pago
}