package com.example.paymentservice;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    // Inyección de dependencia del repositorio
    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment createPayment(Payment payment) {
        // Guardar y devolver el objeto payment
        return paymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> getPaymentById(UUID id) {
        // Buscar un pago por su ID
        return paymentRepository.findById(id);
    }

    @Override
    public List<Payment> getAllPayments() {
        // Devolver la lista de todos los pagos
        return paymentRepository.findAll();
    }

    @Override
    public Payment updatePayment(UUID id, Payment updatedPayment) {
        // Buscar el pago existente y actualizarlo
        return paymentRepository.findById(id)
                .map(existingPayment -> {
                    // Actualización de los campos del pago
                    existingPayment.setAmount(updatedPayment.getAmount());
                    existingPayment.setCurrency(updatedPayment.getCurrency());
                    existingPayment.setStatus(updatedPayment.getStatus());
                    existingPayment.setPaymentMethod(updatedPayment.getPaymentMethod());
                    existingPayment.setPaymentDate(updatedPayment.getPaymentDate());

                    // Guardar el pago actualizado
                    return paymentRepository.save(existingPayment);
                }).orElseThrow(() ->
                        new RuntimeException("Payment no encontrado con el ID: " + id));
    }

    @Override
    public void deletePayment(UUID id) {
        // Validar si el pago existe; si no, lanzar excepción
        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Payment no encontrado con el ID: " + id);
        }
        // Eliminar el pago por ID
        paymentRepository.deleteById(id);
    }
}