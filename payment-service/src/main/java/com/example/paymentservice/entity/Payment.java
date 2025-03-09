package com.example.paymentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Generación automática del ID
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

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime paymentDate = LocalDateTime.now(); // Fecha y hora del pago

    @Column(nullable = false)
    private String customerId; // ID del cliente asociado al pago
}