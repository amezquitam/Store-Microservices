package com.example.paymentservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService; // Servicio no reactivo

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Endpoint reactivo, pero el servicio es tradicional
    @PostMapping
    public Mono<ResponseEntity<Payment>> createPayment(@RequestBody Payment payment) {
        // Convertimos la respuesta del servicio no reactivo a un `Mono`
        return Mono.fromCallable(() -> ResponseEntity.ok(paymentService.createPayment(payment)));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Payment>> getPaymentById(@PathVariable UUID id) {
        return Mono.justOrEmpty(paymentService.getPaymentById(id))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<Payment> getAllPayments() {
        // Convertimos la lista a Flux
        return Flux.fromIterable(paymentService.getAllPayments());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Payment>> updatePayment(@PathVariable UUID id, @RequestBody Payment payment) {
        return Mono.fromCallable(() -> {
            Payment updatedPayment = paymentService.updatePayment(id, payment);
            return updatedPayment != null ? ResponseEntity.ok(updatedPayment) : ResponseEntity.notFound().build();
        });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deletePayment(@PathVariable UUID id) {
        return Mono.fromRunnable(() -> paymentService.deletePayment(id))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }
}