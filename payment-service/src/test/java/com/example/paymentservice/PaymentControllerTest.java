package com.example.paymentservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@WebFluxTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void testCreatePayment() {
        Payment payment = new Payment();
        payment.setAmount(2000.5);
        payment.setCurrency("EUR");

        when(paymentService.createPayment(payment)).thenReturn(payment);
        webTestClient.post()
                .uri("/api/payments")
                .bodyValue(payment)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Payment.class)
                .isEqualTo(payment);

        verify(paymentService, times(1)).createPayment(payment);
    }

    @Test
    void testGetPayments() {
        List<Payment> payments = new ArrayList<>();
        payments.add(new Payment());
        payments.add(new Payment());

        when(paymentService.getAllPayments()).thenReturn(payments);
        webTestClient.get()
                .uri("/api/payments")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Payment.class)
                .isEqualTo(payments);
        verify(paymentService, times(1)).getAllPayments();
    }

    @Test
    void testGetPaymentById() {
        Payment payment = new Payment();
        UUID paymentId = UUID.randomUUID();

        payment.setId(paymentId);

        when(paymentService.getPaymentById(paymentId)).thenReturn(Optional.of(payment));
        webTestClient.get()
                .uri("/api/payments/{id}", paymentId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Payment.class)
                .isEqualTo(payment);
        verify(paymentService, times(1)).getPaymentById(paymentId);

    }

    @Test
    void testUpdatePayment() {
        Payment updatedPayment = new Payment();
        UUID paymentId = UUID.randomUUID();

        updatedPayment.setId(paymentId);
        updatedPayment.setAmount(2000.5);
        updatedPayment.setCurrency("EUR");

        when(paymentService.updatePayment(eq(paymentId),eq(updatedPayment))).thenReturn(updatedPayment);
        webTestClient.put()
                .uri("/api/payments/{id}", paymentId)
                .bodyValue(updatedPayment)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Payment.class)
                .isEqualTo(updatedPayment);

        verify(paymentService, times(1)).updatePayment(eq(paymentId),eq(updatedPayment));
    }
}
